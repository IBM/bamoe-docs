{
  "id": "8353a946-1b92-4352-a529-d7cf52d0b033",
  "name": "itorders-data.place-order-taskform.frm",
  "model": {
    "processName": "place-order",
    "processId": "itorders-data.place-order",
    "name": "process",
    "properties": [
      {
        "name": "caseFile_hwSpec",
        "typeInfo": {
          "type": "BASE",
          "className": "org.jbpm.document.Document",
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
        "name": "caseFile_ordered",
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
      },
      {
        "name": "caseFile_orderInfo",
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
        "name": "CaseId",
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
        "name": "Requestor",
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
      "id": "field_2897352400479343E12",
      "name": "caseFile_hwSpec",
      "label": "CaseFile_hwSpec",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "caseFile_hwSpec",
      "standaloneClassName": "org.jbpm.document.Document",
      "code": "Document",
      "serializedFieldClassName": "org.kie.workbench.common.forms.jbpm.model.authoring.document.definition.DocumentFieldDefinition"
    },
    {
      "id": "field_831172453834416E10",
      "name": "caseFile_ordered",
      "label": "CaseFile_ordered",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "caseFile_ordered",
      "standaloneClassName": "java.lang.Boolean",
      "code": "CheckBox",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.checkBox.definition.CheckBoxFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "itorders-data.place-order-taskform.frm",
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
                  "field_id": "field_2897352400479343E12",
                  "form_id": "8353a946-1b92-4352-a529-d7cf52d0b033"
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
                  "field_id": "field_831172453834416E10",
                  "form_id": "8353a946-1b92-4352-a529-d7cf52d0b033"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}