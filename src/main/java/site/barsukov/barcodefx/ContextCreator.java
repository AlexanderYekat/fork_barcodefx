package site.barsukov.barcodefx;

import javafx.scene.Node;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.context.*;
import site.barsukov.barcodefx.controllers.Controller;
import site.barsukov.barcodefx.model.enums.RemarkCause;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.services.StatisticService;
import site.barsukov.barcodefx.utils.Utils;

import java.io.File;
import java.time.format.DateTimeFormatter;

import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.KM_CHECK_ENABLED;
import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.KM_CHECK_TAIL_ENABLED;
import static site.barsukov.barcodefx.props.JsonSystemProps.SystemProp.DEFAULT_SAVE_FOLDER;
import static site.barsukov.barcodefx.props.JsonSystemProps.SystemProp.USE_DEFAULT_SAVE_FOLDER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextCreator {
    public static final String USER_INN = "userInn";
    public static final String SENDER_INN = "senderInn";
    public static final String EXPORTER_NAME = "exporterName";
    public static final String COUNTRY_OKSM = "countryOksm";
    public static final String PRIMARY_DOC_NUM = "primaryDocNum";
    public static final String VSD_NUMBER = "vsdNumber";

    public static final String WATER_LICENSE = "waterLicense";
    public static final String WATER_LICENSE_BUTTON_TEXT = "Лицензии (ТГ Вода)";
    public static final String IMPORT_DATE = "importDate";
    public static final String PRIMARY_DOC_DATE = "primaryDocDate";

    private static final String ERROR_MESSAGE = "Ошибка построения контекста. Подробная информация в логах.";

    private final PropService propService;
    private final StatisticService statisticService;

    public VvodVOborotCrossborderContext createVoborotCrossborderContext(Controller controller) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            VvodVOborotCrossborderContext result = new VvodVOborotCrossborderContext();
            fillBaseContext(result, controller, null);

            for (Object curItem : controller.crossborderList.getItems()) {
                if (curItem instanceof TextField) {
                    String id = ((TextField) curItem).getId();
                    String text = ((TextField) curItem).getText().trim();
                    switch (id) {
                        case USER_INN:
                            result.setUserINN(text);
                            break;
                        case SENDER_INN:
                            result.setSenderINN(text);
                            break;
                        case EXPORTER_NAME:
                            result.setExporterName(text);
                            break;
                        case COUNTRY_OKSM:
                            result.setCountryOksm(text);
                            break;
                        case PRIMARY_DOC_NUM:
                            result.setPrimaryDocNum(text);
                            break;
                        case VSD_NUMBER:
                            result.setVsdNumber(text);
                            break;
                    }

                } else if (curItem instanceof DatePicker) {
                    String id = ((DatePicker) curItem).getId();
                    switch (id) {
                        case IMPORT_DATE:
                            result.setImportDate(Utils.getLocalDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                        case PRIMARY_DOC_DATE:
                            result.setPrimaryDocDate(Utils.getLocalDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public DataMatrixContext createDataMatrixContext(Controller controller, String fileName) {
        try {
            DataMatrixContext result = new DataMatrixContext();
            fillBaseContext(result, controller, fileName);
            result.setVertical(controller.isVertical.isSelected());
            result.setHorizontal(controller.isHorizontal.isSelected());
            result.setTermoPrinter(controller.sizeTermoPrinter.isSelected());
            result.setHeight(controller.height.getText());
            result.setWidth(controller.width.getText());
            result.setUserLabelText(controller.userLabelText.getText());
            result.setPrintMarkNumber(controller.printMarkNumber.isSelected());
            int markNumberStart = 0;
            try {
                markNumberStart = Integer.parseInt(controller.markNumberStart.getText());
            } catch (Exception e) {
                log.error("Failed to parse markNumberStart {}", controller.markNumberStart.getText());
            }

            result.setMarkNumberStart(markNumberStart);
            result.setTemplateName((String) controller.templatesList.getSelectionModel().getSelectedItem());
            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public AggregationContext createAggregationContext(Controller controller) {
        try {
            AggregationContext result = new AggregationContext();
            fillBaseContext(result, controller);
            result.setDocumentNumber(controller.aggregationDocNum.getText());
            result.setParticipantINN(controller.aggregationINN.getText());
            result.setOrgName(controller.aggregationOrgName.getText());
            result.setSscc(controller.aggregationSccCodeText.getText());
            result.setIp(controller.aggregationIpCB.isSelected());

            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public AcceptanceContext createAcceptanceContext(Controller controller) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            AcceptanceContext result = new AcceptanceContext();
            fillBaseContext(result, controller);
            result.setShipmentId(controller.shipmentId.getText());
            result.setReceiverINN(controller.acceptanceReceiverINN.getText());
            result.setSenderINN(controller.acceptanceSenderINN.getText());
            result.setTransferDate(Utils.getStringDate(controller.acceptanceDate, dateTimeFormatter));
            result.setTransferDateDate(Utils.getLocalDate(controller.acceptanceDate, dateTimeFormatter));

            result.setMoveDocDate(Utils.getStringDate(controller.acceptanceDocDate, dateTimeFormatter));
            result.setMoveDocNum(controller.acceptanceDocNum.getText());
            result.setAcceptanceType((String) controller.acceptanceType.getValue());
            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public WithdrawContext createWithdrawContext(Controller controller) {
        try {
            WithdrawContext result = new WithdrawContext();
            fillBaseContext(result, controller);
            for (Object curItem : controller.withdrawList.getItems()) {
                String curId = ((Node) curItem).getId();
                if ("withdrawKktNumber".equals(curId)) {
                    result.setKktNumber(((TextField) curItem).getText());
                }
                if ("withdrawPrimaryDocumentCustomName".equals(curId)) {
                    result.setPrimaryDocumentCustomName(((TextField) curItem).getText());
                }
                if ("withdrawPrimaryDocumentNumber".equals(curId)) {
                    result.setPrimaryDocumentNumber(((TextField) curItem).getText());
                }
                if ("withdrawPrimaryDocumentDate".equals(curId)) {
                    result.setPrimaryDocumentDate(Utils.getLocalDate((DatePicker) curItem));
                }
                if ("withdrawDate".equals(curId)) {
                    result.setWithdrawalDate(Utils.getLocalDate((DatePicker) curItem));
                }
                if ("withdrawPrimaryDocumentType".equals(curId)) {
                    result.setPrimaryDocumentType((String) ((ComboBox) curItem).getValue());
                }
                if ("withdrawTradeParticipantInn".equals(curId)) {
                    result.setTradeParticipantInn(((TextField) curItem).getText());
                }
                if ("withdrawType".equals(curId)) {
                    result.setWithdrawalType((String) ((ComboBox) curItem).getValue());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public VvodVOborotImportFtsContext createVvodVOborotImportFtsContext(Controller controller) {
        VvodVOborotImportFtsContext result = new VvodVOborotImportFtsContext();
        fillBaseContext(result, controller);
        result.setUserINN(controller.vOborotImportFtsINN.getText());
        result.setDeclarationNum(controller.vOborotImportFtsDtNum.getText());
        result.setDeclarationDate(Utils.getLocalDate(controller.vOborotImportFtsDtDate));
        return result;
    }

    public CancellationContext createCancellationContext(Controller controller) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            CancellationContext result = new CancellationContext();
            fillBaseContext(result, controller);

            for (Object curItem : controller.cancellationList.getItems()) {
                if (curItem instanceof TextField) {
                    String id = ((TextField) curItem).getId();
                    switch (id) {
                        case "participantInn":
                            result.setParticipantINN(((TextField) curItem).getText());
                            break;
                        case "cancellationDocNum":
                            result.setCancellationDocNum(((TextField) curItem).getText());
                            break;
                    }

                } else if (curItem instanceof DatePicker) {
                    String id = ((DatePicker) curItem).getId();
                    switch (id) {
                        case "cancellationDocDate":
                            result.setCancellationDate(Utils.getStringDate(((DatePicker) curItem), dateTimeFormatter));
                            break;

                    }
                } else if (curItem instanceof ComboBox) {
                    String id = ((ComboBox) curItem).getId();
                    switch (id) {
                        case "cancellationReason":
                            String reason = (String) ((ComboBox) curItem).getValue();
                            if (StringUtils.isBlank(reason)) {
                                throw new IllegalArgumentException("Укажите причину списания");
                            }
                            result.setReason(reason);
                            break;
                    }
                }
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            throw e;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public VvodVOborotContext createVvodVOborotContext(Controller controller) {
        try {
            VvodVOborotContext result = new VvodVOborotContext();
            fillBaseContext(result, controller);
            result.setUserINN(controller.userINNText.getText());
            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public ComparatorContext createComparatorContext(Controller controller) {
        try {
            ComparatorContext result = new ComparatorContext();
            fillBaseContext(result, controller);
            result.setComparedFile(new File(controller.comparedFileLabel.getText()));
            result.setIgnoreTails(controller.ignoreCryptoTail.isSelected());
            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }


    public ShipmentContext createShipmentContext(Controller controller) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            ShipmentContext result = new ShipmentContext();
            fillBaseContext(result, controller);
            for (Object curItem : controller.shipmentList.getItems()) {
                if (curItem instanceof TextField) {
                    String id = ((TextField) curItem).getId();
                    switch (id) {
                        case "shipmentReceiverINN":
                            result.setReceiverINN(((TextField) curItem).getText());
                            break;
                        case "shipmentSenderINN":
                            result.setSenderINN(((TextField) curItem).getText());
                            break;
                        case "shipmentDocNum":
                            result.setMoveDocNum(((TextField) curItem).getText());
                            break;
                    }

                } else if (curItem instanceof DatePicker) {
                    String id = ((DatePicker) curItem).getId();
                    switch (id) {
                        case "shipmentDate":
                            result.setTransferDate(Utils.getStringDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                        case "shipmentDocDate":
                            result.setMoveDocDate(Utils.getStringDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                        case "withdrawDate":
                            result.setWithdrawDate(Utils.getStringDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                    }

                } else if (curItem instanceof ComboBox) {
                    String id = ((ComboBox) curItem).getId();
                    switch (id) {
                        case "shipmentType":
                            if (((ComboBox) curItem).getValue() == null) {
                                throw new IllegalArgumentException("Не указан тип отгрузки");
                            }
                            result.setShipmentType((String) ((ComboBox) curItem).getValue());
                            break;
                        case "withdrawType":
                            if (((ComboBox) curItem).getValue() != null) {
                                result.setWithdrawType((String) ((ComboBox) curItem).getValue());
                            }
                            break;
                    }
                } else if (curItem instanceof CheckBox) {
                    String id = ((CheckBox) curItem).getId();
                    switch (id) {
                        case "shipmentNotUOT":
                            result.setShippingToParticipant(!((CheckBox) curItem).isSelected());
                            break;
                    }
                }
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            throw e;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public RemarkContext createRemarkContext(Controller controller) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            RemarkContext result = new RemarkContext();
            fillBaseContext(result, controller);
            for (Object curItem : controller.remarkList.getItems()) {
                if (curItem instanceof TextField) {
                    String id = ((TextField) curItem).getId();
                    switch (id) {
                        case "participantInn":
                            result.setParticipantINN(((TextField) curItem).getText());
                            break;
                        case "remarkTnvd":
                            result.setRemarkTnvd(((TextField) curItem).getText());
                            break;
                        case "remarkColor":
                            result.setRemarkColor(((TextField) curItem).getText());
                            break;
                        case "remarkSize":
                            result.setRemarkSize(((TextField) curItem).getText());
                            break;
                    }

                } else if (curItem instanceof DatePicker) {
                    String id = ((DatePicker) curItem).getId();
                    switch (id) {
                        case "remarkDate":
                            result.setRemarkDate(Utils.getLocalDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                    }

                } else if (curItem instanceof ComboBox) {
                    String id = ((ComboBox) curItem).getId();
                    switch (id) {
                        case "remarkCause":
                            result.setRemarkCause((RemarkCause) ((ComboBox) curItem).getValue());
                            break;
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    public ProductionContext createProductionRFContext(Controller controller) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            ProductionContext result = new ProductionContext();
            fillBaseContext(result, controller);
            for (Object curItem : controller.productionList.getItems()) {
                if (curItem instanceof TextField) {
                    String id = ((TextField) curItem).getId();
                    switch (id) {
                        case "participantInn":
                            result.setParticipantInn(((TextField) curItem).getText());
                            break;
                        case "producerInn":
                            result.setProducerInn(((TextField) curItem).getText());
                            break;
                        case "ownerInn":
                            result.setOwnerInn(((TextField) curItem).getText());
                            break;
                        case "certificateNumber":
                            result.setCertificateNumber(((TextField) curItem).getText());
                            break;
                        case "tnvedCode":
                            result.setTnvedCode(((TextField) curItem).getText().trim());
                            break;
                        case VSD_NUMBER:
                            result.setVsdNumber(((TextField) curItem).getText().trim());
                            break;
                    }

                } else if (curItem instanceof DatePicker) {
                    String id = ((DatePicker) curItem).getId();
                    switch (id) {
                        case "productDate":
                            result.setProductDate(Utils.getLocalDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                        case "certificateDate":
                            result.setCertificateDate(Utils.getLocalDate(((DatePicker) curItem), dateTimeFormatter));
                            break;
                    }

                } else if (curItem instanceof ComboBox) {
                    String id = ((ComboBox) curItem).getId();
                    switch (id) {
                        case "productionOrder":
                            result.setProductionOrder((String) ((ComboBox) curItem).getValue());
                            break;
                        case "certificateType":
                            result.setCertificateType((String) ((ComboBox) curItem).getValue());
                            break;
                    }
                }
            }
            result.setWaterLicenses(controller.getWaterLicenses());

            return result;
        } catch (Exception e) {
            log.error("Error creating context", e);
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).showAndWait();
            throw e;
        }
    }

    private void fillBaseContext(BaseContext context, Controller controller) {
        fillBaseContext(context, controller, null);
    }

    private void fillBaseContext(BaseContext context, Controller controller, String fileName) {
        if (StringUtils.isBlank(fileName)) {
            context.setCsvFileName(controller.fileNameLabel.getText());
        } else {
            context.setCsvFileName(fileName);
        }
        if (propService.getBooleanProp(USE_DEFAULT_SAVE_FOLDER)) {
            context.setResultFolder(propService.getProp(DEFAULT_SAVE_FOLDER));
        } else {
            context.setResultFolder(controller.getResultFolder());
        }
        context.setPropService(propService);
        context.setStatisticService(statisticService);
        context.setRangeEnabled(controller.rangeCheckBox.isSelected());
        context.setRangeFrom(controller.rangeFrom.getText());
        context.setRangeTo(controller.rangeTo.getText());
        context.setValidateKM(propService.getBooleanProp(KM_CHECK_ENABLED));
        context.setValidateKMTail(propService.getBooleanProp(KM_CHECK_TAIL_ENABLED));
        context.setCategory(controller.category.getSelectionModel().getSelectedItem());
    }


}
