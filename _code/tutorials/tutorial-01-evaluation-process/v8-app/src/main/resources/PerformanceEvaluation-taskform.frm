{
  "id": "47078d21-7da5-4d3f-8355-0fcd78b09f39",
  "name": "PerformanceEvaluation-taskform.frm",
  "model": {
    "taskName": "PerformanceEvaluation",
    "processId": "evaluation",
    "name": "task",
    "properties": [
      {
        "name": "BusinessAdministratorId",
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
        "name": "reason",
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
        "name": "performance",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.Integer",
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
      "placeHolder": "Reason",
      "rows": 4,
      "id": "field_332058348325587E12",
      "name": "reason",
      "label": "Reason",
      "required": false,
      "readOnly": true,
      "validateOnChange": true,
      "binding": "reason",
      "standaloneClassName": "java.lang.String",
      "code": "TextArea",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition"
    },
    {
      "placeHolder": "Performance",
      "maxLength": 100,
      "id": "field_336003622256354E12",
      "name": "performance",
      "label": "Performance",
      "required": true,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "performance",
      "standaloneClassName": "java.lang.Integer",
      "code": "IntegerBox",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.integerBox.definition.IntegerBoxFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "PerformanceEvaluation-taskform.frm",
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
                  "field_id": "field_332058348325587E12",
                  "form_id": "47078d21-7da5-4d3f-8355-0fcd78b09f39"
                }
              }
            ]
          }
        ]
      },
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
                  "field_id": "field_336003622256354E12",
                  "form_id": "47078d21-7da5-4d3f-8355-0fcd78b09f39"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}