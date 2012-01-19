dependencies = {
  layers: [
    {
      name: "ambra.js",
      layerDependencies: [
      ],
      dependencies: [
        "dojo.cookie",
        "dijit.layout.ContentPane",
        "dijit.Dialog",
        "dijit.layout.TabContainer",
        "dojox.data.dom",
        "ambra.general",
        "ambra.domUtil",
        "ambra.htmlUtil",
        "ambra.formUtil",
        "ambra.widget.LoadingCycle",
        "ambra.widget.RegionalDialog",
        "ambra.navigation",
        "ambra.horizontalTabs",
        "ambra.floatMenu",
        "ambra.annotation",
        "ambra.corrections",
        "ambra.displayComment",
        "ambra.responsePanel",
        "ambra.rating",
        "ambra.slideshow"
      ]
    }
  ],

  prefixes: [
    [ "dijit", "../dijit" ],
    [ "dojox", "../dojox" ],
    [ "ambra", "../../ambra" ]
  ]
}
