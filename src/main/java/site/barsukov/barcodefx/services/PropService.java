package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.model.PickingServer;
import site.barsukov.barcodefx.model.PropDto;
import site.barsukov.barcodefx.model.converter.PropsConverter;
import site.barsukov.barcodefx.model.dao.SysProp;
import site.barsukov.barcodefx.model.enums.SysPropType;
import site.barsukov.barcodefx.props.*;
import site.barsukov.barcodefx.repository.SysPropRepo;
import site.barsukov.barcodefx.serializer.IPropKeyDeserializer;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.props.JsonSystemProps.SystemProp.SERVERS_PARAMS;
import static site.barsukov.barcodefx.props.JsonUpdateProps.UpdateProp.*;

@Service
@RequiredArgsConstructor
public class PropService {
    static final Logger logger = Logger.getLogger(PropService.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final SysPropRepo sysPropRepo;
    private final PropsConverter propsConverter;

    private static SecretKeySpec createSecretKey(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] salt = new String("12345678").getBytes();

        // Decreasing this speeds down startup time and can be useful during testing, but it also makes it easier for brute force attackers
        int iterationCount = 40000;
        // Other values give me java.security.InvalidKeyException: Illegal key size or default parameters
        int keyLength = 128;

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    private static String encrypt(String property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(property.getBytes("UTF-8"));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }

    private static String encrypt(String property, char[] password) throws GeneralSecurityException, UnsupportedEncodingException {
        return encrypt(property, createSecretKey(password));
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String decrypt(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
        String iv = string.split(":")[0];
        String property = string.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static String decrypt(String string, char[] password) throws GeneralSecurityException, IOException {
        return decrypt(string, createSecretKey(password));
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }

    @PostConstruct
    private void PropServiceInitialize() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleModule simpleKeyModule = new SimpleModule();
        simpleKeyModule.addKeyDeserializer(IProp.class, new IPropKeyDeserializer());
        mapper.registerModule(simpleKeyModule);
    }

    public List<PickingServer> getServers() throws IOException, GeneralSecurityException {
        String serverProps = getProp(SERVERS_PARAMS);
        if (StringUtils.isBlank(serverProps)) {
            return new ArrayList<>();
        }
        return decryptServerPasswords(mapper.readValue(serverProps, new TypeReference<>() {
        }), getProp(CLIENT_ID).toCharArray());
    }

    public void saveServers(List<PickingServer> servers) throws JsonProcessingException, GeneralSecurityException, UnsupportedEncodingException {
        encryptServerPasswords(servers, getProp(CLIENT_ID).toCharArray());
        saveProp(SERVERS_PARAMS, mapper.writeValueAsString(servers));
    }

    public Set<PropDto> exportProps() {
        return sysPropRepo.findAll().stream().map(propsConverter::toDto).collect(Collectors.toSet());
    }

    public void importProps(File file) {
        Set<PropDto> props;
        try {
            String propsline = FileUtils.readFileToString(file, "UTF-8");
            props = mapper.readValue(propsline, new TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error("Error reading props", e);
            props = Collections.emptySet();
        }

        if (props.isEmpty()) {
            return;
        }
        sysPropRepo.deleteAll();
        props.stream().map(propsConverter::toEntity).forEach(sysPropRepo::save);
    }

    public void saveProp(IProp prop, String value) {
        Optional<SysProp> foundProp = sysPropRepo.getFirstBySysKey(prop.name());

        if (foundProp.isPresent()) {
            var existingProp = foundProp.get();
            existingProp.setValue(value);
            sysPropRepo.save(existingProp);
            return;
        }

        SysProp newProp = propsConverter.toEntity(prop, value);
        sysPropRepo.save(newProp);
    }

    public void saveProp(PropDto propDto) {
        Optional<SysProp> foundProp = sysPropRepo.getFirstBySysKey(propDto.getName());

        if (foundProp.isPresent()) {
            var existingProp = foundProp.get();
            existingProp.setValue(propDto.getValue());
            sysPropRepo.save(existingProp);
            return;
        }
        sysPropRepo.save(propsConverter.toEntity(propDto));
    }

    public Set<PropDto> getPropsByType(SysPropType type) {
        return sysPropRepo.getByType(type).stream()
                .map(propsConverter::toDto)
                .collect(Collectors.toSet());
    }

    public void saveProp(IProp prop, boolean value) {
        saveProp(prop, Boolean.toString(value));
    }

    public String getProp(IProp prop) {
        Optional<SysProp> found = sysPropRepo.getFirstBySysKey(prop.name());
        if (found.isPresent()) {
            return found.get().getValue();
        }
        sysPropRepo.save(propsConverter.toEntity(prop, prop.getDefaultValue()));
        return prop.getDefaultValue();
    }

    public boolean getBooleanProp(IProp prop) {
        return Boolean.parseBoolean(getProp(prop));
    }

    public Float getFloatProp(IProp prop) {
        return Float.parseFloat(getProp(prop));
    }

    public Integer getIntegerProp(IProp prop) {
        return Integer.parseInt(getProp(prop));
    }

    public void saveProps(Set<PropDto> props, File file) {
        try {
            mapper.writeValue(file, props);
        } catch (IOException e) {
            logger.error("Ошибка сохранения настроек", e);
        }
    }

    private List<PickingServer> decryptServerPasswords(List<PickingServer> readValue, char[] password) throws GeneralSecurityException, IOException {
        for (PickingServer server : readValue) {
            if (StringUtils.isNotBlank(server.getPassword())) {
                server.setPassword(decrypt(server.getPassword(), password));
            }
        }
        return readValue;
    }

    private void encryptServerPasswords(List<PickingServer> servers, char[] password) throws GeneralSecurityException, UnsupportedEncodingException {
        for (PickingServer server : servers) {
            if (StringUtils.isNotBlank(server.getPassword())) {
                server.setPassword(encrypt(server.getPassword(), password));
            }
        }
    }
}
