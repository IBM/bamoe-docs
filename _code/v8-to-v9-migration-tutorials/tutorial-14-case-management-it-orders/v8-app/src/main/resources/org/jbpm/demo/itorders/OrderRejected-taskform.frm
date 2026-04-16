{
  "id": "46ab0b83-b462-45df-a710-0bc982ad2f66",
  "name": "OrderRejected-taskform.frm",
  "model": {
    "taskName": "OrderRejected",
    "processId": "itorders.orderhardware",
    "name": "task",
    "properties": [
      {
        "name": "_reason",
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
        "name": "_supplierComment",
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
      }
    ],
    "formModelType": "org.kie.workbench.common.forms.jbpm.model.authoring.task.TaskFormModel"
  },
  "fields": [
    {
      "placeHolder": "Reason",
      "rows": 4,
      "id": "field_258688802961102E11",
      "name": "_reason",
      "label": "Reason",
      "required": false,
      "readOnly": true,
      "validateOnChange": true,
      "binding": "_reason",
      "standaloneClassName": "java.lang.String",
      "code": "TextArea",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition"
    },
    {
      "placeHolder": "Supplier Comment",
      "rows": 4,
      "id": "field_712942388989199E11",
      "name": "_supplierComment",
      "label": "Supplier Comment",
      "required": false,
      "readOnly": true,
      "validateOnChange": true,
      "binding": "_supplierComment",
      "standaloneClassName": "java.lang.String",
      "code": "TextArea",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "OrderRejected-taskform.frm",
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
                  "field_id": "field_258688802961102E11",
                  "form_id": "46ab0b83-b462-45df-a710-0bc982ad2f66"
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
                  "field_id": "field_712942388989199E11",
                  "form_id": "46ab0b83-b462-45df-a710-0bc982ad2f66"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}