{
  "id": "b7cf9793-c0a6-4a3d-ad00-ff198ef8abd3",
  "name": "ManagerApproval-taskform.frm",
  "model": {
    "taskName": "ManagerApproval",
    "processId": "itorders.orderhardware",
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
        "name": "approved_",
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
        "name": "managerComment_",
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
    "formModelType": "org.kie.workbench.common.forms.jbpm.model.authoring.task.TaskFormModel"
  },
  "fields": [
    {
      "id": "field_536454335984558E11",
      "name": "_hwSpec",
      "label": "Recommended specification",
      "required": false,
      "readOnly": true,
      "validateOnChange": true,
      "binding": "_hwSpec",
      "standaloneClassName": "org.jbpm.document.Document",
      "code": "Document",
      "serializedFieldClassName": "org.kie.workbench.common.forms.jbpm.model.authoring.document.definition.DocumentFieldDefinition"
    },
    {
      "id": "field_624363590125407E11",
      "name": "approved_",
      "label": "Approved",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "approved_",
      "standaloneClassName": "java.lang.Boolean",
      "code": "CheckBox",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.checkBox.definition.CheckBoxFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "ManagerApproval-taskform.frm",
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
                  "field_id": "field_536454335984558E11",
                  "form_id": "b7cf9793-c0a6-4a3d-ad00-ff198ef8abd3"
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
                  "field_id": "field_624363590125407E11",
                  "form_id": "b7cf9793-c0a6-4a3d-ad00-ff198ef8abd3"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}