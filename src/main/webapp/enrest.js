"use strict";

console.log( "Enrest in peace" );

var enrest = {};

enrest.newPath = function( oldPath, pathRef, pathRefValue ) {
   return oldPath.replace( "{:" + pathRef + "}", pathRefValue );
}

enrest.fixPathSpan = function( spanId, newPath ) {
   document.getElementById( spanId ).innerHTML = newPath;
}

enrest.fixFormAction = function( formId, newPath ) {
   document.getElementById( formId ).action = newPath;
}
enrest.updateSpanAndForm = function( no, basePathSpanId, pathSpanId, formId ) {
   console.log( "Update span and form", no, basePathSpanId, pathSpanId, formId );
   console.log( "Update span and form", no.value );
//   var oldPath = document.getElementById( formId ).action;
   var oldPath = document.getElementById( basePathSpanId ).innerHTML;
   var newPath = enrest.newPath( oldPath, "id", no.value );
   enrest.fixFormAction( formId, newPath );
   enrest.fixPathSpan( pathSpanId, newPath );
}

enrest.x = function( jqElement ) {
    var objName = jqElement.attr("typeOf");
    var props = jqElement.find( "*[property]" ).map( function() {
        return {
            "name": this.getAttribute("property"),
            "value": this.innerHTML
        };
    } ).get();

//    for( var i = 0; i < props.length; i++ ) {
//        props[i].value = jqElement.find( "dd[" + i + "]").innerHTML;
//    }

    var obj = {
        "_typeOf": objName,
        "_properties": props
    };

    props.forEach( function( p ) { obj[p.name] =  p.value; } );

    return obj;
};

enrest.currentObjects = $('*[typeOf]').map( function() { return enrest.x( $(this) ); } ).get();

document.write( "<div>JSON Debugging: <pre>" + JSON.stringify( enrest.currentObjects, null, '  ' ) + "</pre></div>");

//$('*[data-object-type]').each( function(x) { console.log( x + ": " + this.getAttribute("data-object-type") )} );