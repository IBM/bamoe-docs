{
  "id": "209ee0d8-ec69-463d-b848-0a93fd2c602f",
  "name": "Survey",
  "model": {
    "className": "org.jbpm.demo.itorders.Survey",
    "name": "survey",
    "properties": [
      {
        "name": "satisfied",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.Boolean",
          "multiple": false
        },
        "metaData": {
          "entries": []
        }
      },
      {
        "name": "deliveredOnTime",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.Boolean",
          "multiple": false
        },
        "metaData": {
          "entries": []
        }
      },
      {
        "name": "missingEquipment",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.String",
          "multiple": false
        },
        "metaData": {
          "entries": []
        }
      },
      {
        "name": "comment",
        "typeInfo": {
          "type": "BASE",
          "className": "java.lang.String",
          "multiple": false
        },
        "metaData": {
          "entries": []
        }
      }
    ],
    "formModelType": "org.kie.workbench.common.forms.data.modeller.model.DataObjectFormModel"
  },
  "fields": [
    {
      "id": "field_1061294778185306E11",
      "name": "satisfied",
      "label": "Satisfied",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "satisfied",
      "standaloneClassName": "java.lang.Boolean",
      "code": "CheckBox",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.checkBox.definition.CheckBoxFieldDefinition"
    },
    {
      "id": "field_0230569808308707E12",
      "name": "deliveredOnTime",
      "label": "Delivered on time?",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "deliveredOnTime",
      "standaloneClassName": "java.lang.Boolean",
      "code": "CheckBox",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.checkBox.definition.CheckBoxFieldDefinition"
    },
    {
      "placeHolder": "Missing Equipment",
      "rows": 4,
      "id": "field_0988658517175382E12",
      "name": "missingEquipment",
      "label": "Missing Equipment",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "missingEquipment",
      "standaloneClassName": "java.lang.String",
      "code": "TextArea",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition"
    },
    {
      "placeHolder": "Comment",
      "rows": 4,
      "id": "field_568410077170925E11",
      "name": "comment",
      "label": "Comment",
      "required": false,
      "readOnly": false,
      "validateOnChange": true,
      "binding": "comment",
      "standaloneClassName": "java.lang.String",
      "code": "TextArea",
      "serializedFieldClassName": "org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 2,
    "name": "Survey",
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
                  "field_id": "field_1061294778185306E11",
                  "form_id": "209ee0d8-ec69-463d-b848-0a93fd2c602f"
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
                  "field_id": "field_0230569808308707E12",
                  "form_id": "209ee0d8-ec69-463d-b848-0a93fd2c602f"
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
                  "field_id": "field_0988658517175382E12",
                  "form_id": "209ee0d8-ec69-463d-b848-0a93fd2c602f"
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
                  "field_id": "field_568410077170925E11",
                  "form_id": "209ee0d8-ec69-463d-b848-0a93fd2c602f"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}