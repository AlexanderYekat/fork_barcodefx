package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import site.barsukov.barcodefx.exception.AppException;
import site.barsukov.barcodefx.utils.Utils;
import site.barsukov.barcodefx.model.ArchivedPickingOrder;
import site.barsukov.barcodefx.model.BcfxFtpClient;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.PickingServer;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.model.yo.pickingorder.CodesTypeType;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;
import site.barsukov.barcodefx.model.yo.pickingorder.ProductGroupType;
import site.barsukov.barcodefx.serializer.XMLCalendarModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.utils.Utils.getXMLGregorianCalendarNow;

public class PickingOrderService {
    public static final String PICKING_ORDERS_ROOT_PATH = "data" + File.separatorChar + "orders" + File.separatorChar;
    public static final String LOCAL_PICKING_ORDERS_PATH = "data" + File.separatorChar + "orders" + File.separatorChar + "local" + File.separatorChar;
    public static final String REMOTE_PICKING_ORDERS_PATH = "data" + File.separatorChar + "orders" + File.separatorChar + "remote" + File.separatorChar;
    public static final String LOCAL_PICKING_ORDERS_ARCHIVE_PATH = "data" + File.separatorChar + "orders" + File.separatorChar + "local_archive" + File.separatorChar;
    private static final XmlMapper xmlMapper = new XmlMapper();

    public static File savePickingOrder(File file, PickingOrder pickingOrder) throws IOException {
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        xmlMapper.registerModule(new XMLCalendarModule());
        xmlMapper.registerModule(module);

        List<String> lines = FileUtils.readLines(file, "UTF-8");

        pickingOrder.setFileName(FilenameUtils.getName(file.getAbsolutePath()));
        pickingOrder.setCisCount(lines.size());
//        pickingOrder.setState(state);
//        pickingOrder.setComment(comment);
        pickingOrder.setMd5(getMd5(file));
//        pickingOrder.setSscc();
//        pickingOrder.setCodesType(getType(lines, category));
        pickingOrder.setCreationDate(getXMLGregorianCalendarNow());
        pickingOrder.setModificationDate(getXMLGregorianCalendarNow());
//        pickingOrder.setSecurity();
//        pickingOrder.setProductGroup(convertTg(category));
//        pickingOrder.setVersion(BigDecimal.ONE);

        File destCodeFile = new File(LOCAL_PICKING_ORDERS_PATH + pickingOrder.getFileName());
        if (!file.getAbsolutePath().equals(destCodeFile.getAbsolutePath())) {
            FileUtils.copyFile(file, destCodeFile);
        }

        File pickingOrderFile = new File(LOCAL_PICKING_ORDERS_PATH + "meta_" + FilenameUtils.getBaseName(file.getAbsolutePath()) + ".xml");
        FileUtils.writeStringToFile(pickingOrderFile, xmlMapper.writeValueAsString(pickingOrder), "UTF-8");
        return destCodeFile;
    }

    public static File getFileWithCodes(PickingOrder pickingOrder) {
        return new File(LOCAL_PICKING_ORDERS_PATH + pickingOrder.getFileName());
    }

    public static void updatePickingOrderMeta(String filename, String newStatus) throws IOException {
        var pickingOrderFile = new File(LOCAL_PICKING_ORDERS_PATH + "meta_" + FilenameUtils.getBaseName(filename) + ".xml");
        var resultPickingOrder = xmlMapper.readValue(FileUtils.readFileToString(pickingOrderFile, "UTF-8"), PickingOrder.class);
        resultPickingOrder.setState(newStatus);
        FileUtils.writeStringToFile(pickingOrderFile, xmlMapper.writeValueAsString(resultPickingOrder), "UTF-8");
    }

    public static boolean isOrderExist(File file) {
        String fileName = FilenameUtils.getName(file.getAbsolutePath());
        return getLocalPickingOrders().stream().anyMatch(s -> fileName.equals(s.getFileName()));
    }

    public static List<PickingOrder> getLocalPickingOrders() {

        return getPickingOrdersFromPath(LOCAL_PICKING_ORDERS_PATH);
    }

    public static ProductGroupType convertTg(GoodCategory category) {
        switch (category) {
            case SHOES:
                return ProductGroupType.SHOES;
            case ATP:
                return ProductGroupType.OTP;
            case TIRES:
                return ProductGroupType.TIRES;
            case CLOTHES:
                return ProductGroupType.CLOTHES;
            case MILK:
                return ProductGroupType.MILK;
            case PERFUMERY:
                return ProductGroupType.PERFUMERY;
            default:
                return null;
        }
    }

    private static CodesTypeType getType(List<String> lines, GoodCategory category) {
        boolean isKi = false;
        for (String curString : lines) {
            KM km = BaseDocService.createKM(curString, category);
            if (km.getSuzString().length() == km.getGtin().length() + km.getSerial().length()) {
                isKi = true;
            } else if (isKi) {
                throw new IllegalArgumentException("Не удалось определить тип кодов. В файле содержатся и коды маркировки и коды идентификации ");
            }
        }
        return isKi ? CodesTypeType.KI : CodesTypeType.KM;
    }

    private static String getMd5(File file) {
        try (InputStream is = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        } catch (IOException e) {
            throw new IllegalArgumentException("Ошибка подсчета md5 для файла " + file.getAbsolutePath());
        }
    }

    public static GoodCategory convertTg(ProductGroupType productGroup) {
        switch (productGroup) {
            case SHOES:
                return GoodCategory.SHOES;
            case OTP:
                return GoodCategory.ATP;
            case TIRES:
                return GoodCategory.TIRES;
            case CLOTHES:
                return GoodCategory.CLOTHES;
            case MILK:
                return GoodCategory.MILK;
            case PERFUMERY:
                return GoodCategory.PERFUMERY;
            default:
                return null;
        }
    }

    public static void archivePickingOrder(PickingOrder selectedItem) throws ZipException {
        File codeFile = new File(LOCAL_PICKING_ORDERS_PATH + File.separatorChar + selectedItem.getFileName());
        File metaFile = new File(LOCAL_PICKING_ORDERS_PATH + File.separatorChar + "meta_" + FilenameUtils.getBaseName(selectedItem.getFileName()) + ".xml");

        new ZipFile(LOCAL_PICKING_ORDERS_ARCHIVE_PATH + File.separatorChar + selectedItem.getFileName() + ".zms")
                .addFiles(Arrays.asList(codeFile, metaFile));
        codeFile.delete();
        metaFile.delete();

    }

    public static List<PickingOrder> getRemotePickingOrders() throws IOException {

        return getPickingOrdersFromPath(REMOTE_PICKING_ORDERS_PATH);
    }

    private static List<PickingOrder> getPickingOrdersFromPath(String path) {
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        xmlMapper.registerModule(new XMLCalendarModule());
        xmlMapper.registerModule(module);

        List<PickingOrder> result = new ArrayList<>();
        List<File> files = FileUtils.listFiles(new File(path), new String[]{"xml"}, false)
                .stream().filter(s -> FilenameUtils.getBaseName(s.getAbsolutePath()).startsWith("meta_"))
                .collect(Collectors.toList());
        for (File curFile : files) {
            try {
                result.add(xmlMapper.readValue(FileUtils.readFileToString(curFile, "UTF-8"), PickingOrder.class));
            } catch (IOException e) {
                throw new AppException(e);
            }
        }
        return result;
    }

    public static List<ArchivedPickingOrder> getLocalArchivePickingOrders() throws IOException {
        List<File> files = FileUtils.listFiles(new File(LOCAL_PICKING_ORDERS_ARCHIVE_PATH), new String[]{"zms"}, false)
                .stream().collect(Collectors.toList());
        List<ArchivedPickingOrder> result = new ArrayList<>();

        for (File curFile : files) {
            Map<String, Object> attributes = Files.readAttributes(curFile.toPath(), "creationTime");
            result.add(new ArchivedPickingOrder(FilenameUtils.getName(curFile.getAbsolutePath()),
                    fileTimeToDate(attributes.get("creationTime"))));
        }
        return result;
    }

    private static Date fileTimeToDate(Object fileTimeObj) {
        FileTime fileTime = (FileTime) fileTimeObj;
        return new Date(fileTime.toMillis());
    }

    public static void removeArchivedOrders(List<ArchivedPickingOrder> selectedArchivedPickingOrders) {
        for (ArchivedPickingOrder curOrder : selectedArchivedPickingOrders) {
            File tempFile = new File(LOCAL_PICKING_ORDERS_ARCHIVE_PATH + curOrder.getFileName());
            tempFile.delete();
        }
    }

    public static void returnFromArchivePickingOrder(List<ArchivedPickingOrder> selectedItems) throws ZipException {
        for (ArchivedPickingOrder curOrder : selectedItems) {
            File curFile = new File(LOCAL_PICKING_ORDERS_ARCHIVE_PATH + curOrder.getFileName());
            ZipFile zipFile = new ZipFile(curFile.getAbsoluteFile());

            zipFile.extractAll(LOCAL_PICKING_ORDERS_PATH);
            curFile.delete();
        }
    }

    public static void clearRemoteCache() throws IOException {
        FileUtils.cleanDirectory(new File(REMOTE_PICKING_ORDERS_PATH));
    }

    public static void getRemoteCash(PickingServer pickingServer) throws IOException {
        if (pickingServer.getType() == PickingServer.ServerType.FOLDER) {
            File remoteFolder = new File(pickingServer.getPath());
            List<File> remoteFiles = FileUtils.listFiles(remoteFolder, new String[]{"xml"}, false).stream().collect(Collectors.toList());
            for (File curFile : remoteFiles) {
                FileUtils.copyFile(curFile, new File(REMOTE_PICKING_ORDERS_PATH
                        + FilenameUtils.getName(curFile.getAbsolutePath())));
            }
        } else if (pickingServer.getType() == PickingServer.ServerType.FTP ||
                pickingServer.getType() == PickingServer.ServerType.Z_CLOUD
        ) {

            BcfxFtpClient ftpClient = new BcfxFtpClient(pickingServer);
            ftpClient.open();
            List<String> remoteFiles = ftpClient.getFilesList()
                    .stream()
                    .filter(s -> s.startsWith("meta_"))
                    .filter(s -> s.endsWith(".xml"))
                    .collect(Collectors.toList());
            for (String curFile : remoteFiles) {
                ftpClient.downloadFile(curFile, new File(REMOTE_PICKING_ORDERS_PATH + curFile));
            }
            ftpClient.close();
        }
    }

    public static void removeRemoteOrders(PickingServer pickingServer, List<PickingOrder> selectedItems) throws IOException {
        if (pickingServer.getType() == PickingServer.ServerType.FOLDER) {
            for (PickingOrder curOrder : selectedItems) {
                File remoteMetaFile = new File(pickingServer.getPath() + File.separatorChar + "meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml");
                File remoteDataFile = new File(pickingServer.getPath() + File.separatorChar + curOrder.getFileName());
                FileUtils.deleteQuietly(remoteMetaFile);
                FileUtils.deleteQuietly(remoteDataFile);
            }

        } else if (pickingServer.getType() == PickingServer.ServerType.FTP ||
                pickingServer.getType() == PickingServer.ServerType.Z_CLOUD) {
            BcfxFtpClient ftpClient = new BcfxFtpClient(pickingServer);
            ftpClient.open();
            for (PickingOrder curOrder : selectedItems) {
                ftpClient.deleteFile("meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml");
                ftpClient.deleteFile(curOrder.getFileName());
            }
            ftpClient.close();
        }

    }

    public static void downloadRemoteOrders(PickingServer pickingServer, List<PickingOrder> selectedItems) throws IOException {
        if (pickingServer.getType() == PickingServer.ServerType.FOLDER) {
            for (PickingOrder curOrder : selectedItems) {
                File remoteMetaFile = new File(pickingServer.getPath() + File.separatorChar + "meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml");
                File localMetaFile = new File(LOCAL_PICKING_ORDERS_PATH + "meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml");
                File remoteDataFile = new File(pickingServer.getPath() + File.separatorChar + curOrder.getFileName());
                File localDataFile = new File(LOCAL_PICKING_ORDERS_PATH + curOrder.getFileName());
                FileUtils.copyFile(remoteMetaFile, localMetaFile);
                FileUtils.copyFile(remoteDataFile, localDataFile);
            }

        } else if (pickingServer.getType() == PickingServer.ServerType.FTP ||
                pickingServer.getType() == PickingServer.ServerType.Z_CLOUD) {
            BcfxFtpClient ftpClient = new BcfxFtpClient(pickingServer);
            ftpClient.open();
            for (PickingOrder curOrder : selectedItems) {
                String metaFileName = "meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml";
                String dataFileName = curOrder.getFileName();

                File localMetaFile = new File(LOCAL_PICKING_ORDERS_PATH + metaFileName);
                File localDataFile = new File(LOCAL_PICKING_ORDERS_PATH + dataFileName);

                Utils.checkArgument(ftpClient.downloadFile(metaFileName, localMetaFile), "Ошибка получения файла с FTP");
                Utils.checkArgument(ftpClient.downloadFile(dataFileName, localDataFile), "Ошибка получения файла с FTP");
            }
            ftpClient.close();
        }
    }

    public static void sendOrdersToRemoteServer(PickingServer pickingServer, List<PickingOrder> pickingOrderList) throws IOException {
        if (pickingServer.getType() == PickingServer.ServerType.FOLDER) {
            for (PickingOrder curOrder : pickingOrderList) {
                File remoteMetaFile = new File(pickingServer.getPath() + File.separatorChar + "meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml");
                File localMetaFile = new File(LOCAL_PICKING_ORDERS_PATH + "meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml");
                File remoteDataFile = new File(pickingServer.getPath() + File.separatorChar + curOrder.getFileName());
                File localDataFile = new File(LOCAL_PICKING_ORDERS_PATH + curOrder.getFileName());
                FileUtils.copyFile(localMetaFile, remoteMetaFile);
                FileUtils.copyFile(localDataFile, remoteDataFile);
            }

        } else if (pickingServer.getType() == PickingServer.ServerType.FTP ||
                pickingServer.getType() == PickingServer.ServerType.Z_CLOUD) {
            BcfxFtpClient ftpClient = new BcfxFtpClient(pickingServer);
            ftpClient.open();
            for (PickingOrder curOrder : pickingOrderList) {
                File localMetaFile = new File(LOCAL_PICKING_ORDERS_PATH + "meta_" + FilenameUtils.getBaseName(curOrder.getFileName()) + ".xml");
                File localDataFile = new File(LOCAL_PICKING_ORDERS_PATH + curOrder.getFileName());
                Utils.checkArgument(ftpClient.sendFile(localDataFile), "Ошибка отправки файла на FTP");
                Utils.checkArgument(ftpClient.sendFile(localMetaFile), "Ошибка отправки файла на FTP");
            }
            ftpClient.close();
        }
    }

    public static boolean isPickingOrder(String csvFileName) {
        File sourceFile = new File(csvFileName);
        File orderFolder = new File(LOCAL_PICKING_ORDERS_PATH);
        return orderFolder.getAbsolutePath().equals(sourceFile.getParent());
    }
}
