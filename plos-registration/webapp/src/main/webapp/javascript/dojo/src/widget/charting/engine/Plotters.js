dojo.provide("dojo.widget.charting.engine.Plotters");

/*	
 *	Plotters is the placeholder; what will happen is that the proper renderer types
 *	will be mixed into this object (as opposed to creating a new one).
 */

dojo["requireIf"](dojo.render.svg.capable, "dojo.widget.charting.engine.svg.Plotters");
dojo["requireIf"](!dojo.render.svg.capable && dojo.render.vml.capable, "dojo.widget.charting.engine.vml.Plotters");
