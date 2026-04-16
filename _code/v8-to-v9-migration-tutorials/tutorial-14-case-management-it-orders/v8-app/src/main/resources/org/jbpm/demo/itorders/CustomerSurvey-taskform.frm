{
  "id": "e8ab93a0-526d-45ee-b9c5-8704405caa98",
  "name": "CustomerSurvey-taskform.frm",
  "model": {
    "taskName": "CustomerSurvey",
    "processId": "itorders.orderhardware",
    "name": "task",
    "properties": [
      {
        "name": "orderNumber",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.String",
          "multiple": false
        },
        "metaData": {
          "entries": [
            {
              "name": "field-readOnly",
              "value": true
            }
          ]
        }
      },
      {
        "name": "survey_",
        "typeInfo": {
          "type": "OBJECT",
          "className": "org.jbpm.demo.itorders.Survey",
          "multiple": false
        },
        "metaData": {
          "entries": [
            {
              "name": "field-readOnly",
              "value": false
            }
          ]
        }
      }
    ],
    "formModelType": "org.kie.workbench.common.forms.jbpm.model.authoring.task.TaskFormModel"
  },
  "fields": [
    {
      "nestedForm": "209ee0d8-ec69-463d-b848-0a93fd2c602f",
      "id": "field_910157350791078E11",
      "name": "survey_",
      "label": "Survey",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "survey_",
      "standaloneClassName": "org.jbpm.demo.itorders.Survey",
      "code": "SubForm",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.subForm.definition.SubFormFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "CustomerSurvey-taskform.frm",
    "style": "FLUID",
    "layoutProperties": {},
    "rows": [
      {
        "height": "12",
        "layoutColumns": [
          {
            "span": "12",
            "height": "12",
            "rows": [],
            "layoutComponents": [
              {
                "dragTypeName": "org.kie.workbench.common.forms.editor.client.editor.rendering.EditorFieldLayoutComponent",
                "properties": {
                  "field_id": "field_910157350791078E11",
                  "form_id": "e8ab93a0-526d-45ee-b9c5-8704405caa98"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}