{
  "id": "d1e6dd47-b24c-4f93-ba25-337832926113",
  "name": "evaluation-taskform.frm",
  "model": {
    "processName": "Evaluation",
    "processId": "evaluation",
    "name": "process",
    "properties": [
      {
        "name": "employee",
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
        "name": "initiator",
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
              "value": false
            }
          ]
        }
      }
    ],
    "formModelType": "org.kie.workbench.common.forms.jbpm.model.authoring.process.BusinessProcessFormModel"
  },
  "fields": [
    {
      "maxLength": 100,
      "placeHolder": "Employee",
      "id": "field_740177746345817E11",
      "name": "employee",
      "label": "Employee",
      "required": true,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "employee",
      "standaloneClassName": "java.lang.String",
      "code": "TextBox",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.definition.TextBoxFieldDefinition"
    },
    {
      "placeHolder": "Reason",
      "rows": 4,
      "id": "field_282038126127015E11",
      "name": "reason",
      "label": "Reason",
      "required": true,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "reason",
      "standaloneClassName": "java.lang.String",
      "code": "TextArea",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "evaluation-taskform.frm",
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
                  "field_id": "field_740177746345817E11",
                  "form_id": "d1e6dd47-b24c-4f93-ba25-337832926113"
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
                  "field_id": "field_282038126127015E11",
                  "form_id": "d1e6dd47-b24c-4f93-ba25-337832926113"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}