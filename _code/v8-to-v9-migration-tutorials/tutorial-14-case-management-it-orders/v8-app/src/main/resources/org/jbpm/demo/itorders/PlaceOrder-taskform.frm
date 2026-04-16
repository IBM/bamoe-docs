{
  "id": "32ee0035-d9e5-45e1-99a6-1693a57f8f52",
  "name": "PlaceOrder-taskform.frm",
  "model": {
    "taskName": "PlaceOrder",
    "processId": "itorders-data.place-order",
    "name": "task",
    "properties": [
      {
        "name": "_hwSpec",
        "typeInfo": {
          "type": "BASE",
          "className": "org.jbpm.document.Document",
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
        "name": "requestor",
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
        "name": "info_",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.String",
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
      },
      {
        "name": "ordered_",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.Boolean",
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
      "id": "field_0701908084482717E12",
      "name": "_hwSpec",
      "label": "To be ordered",
      "required": false,
      "readOnly": true,
      "validateOnChange": true,
      "binding": "_hwSpec",
      "standaloneClassName": "org.jbpm.document.Document",
      "code": "Document",
      "serializedFieldClassName": "org.kie.workbench.common.forms.jbpm.model.authoring.document.definition.DocumentFieldDefinition"
    },
    {
      "id": "field_3870836403338484E12",
      "name": "ordered_",
      "label": "Is order placed",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "ordered_",
      "standaloneClassName": "java.lang.Boolean",
      "code": "CheckBox",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.checkBox.definition.CheckBoxFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "PlaceOrder-taskform.frm",
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
                  "field_id": "field_0701908084482717E12",
                  "form_id": "32ee0035-d9e5-45e1-99a6-1693a57f8f52"
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
                  "field_id": "field_3870836403338484E12",
                  "form_id": "32ee0035-d9e5-45e1-99a6-1693a57f8f52"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}