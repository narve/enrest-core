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


//$('*[data-object-type]').each( function(x) { console.log( x + ": " + this.getAttribute("data-object-type") )} );