var callCount = 0;
var admob_vars = {}; var toBeEval = '';

function debugMessage(message) { 
	document.getElementById('debugMessage').innerHTML += message+'<br/><br/>'; 
} 

function jsIncluded() { 
	try {
		if (toBeEval != '') { 
		eval(toBeEval); 
		}
	} 
	catch (e) { div.innerHTML = "Ad: "+e; }
}

function setAndExecute(divId, array) {
	try { 
		for (var j=0;j<array.length;j++) { 
			callCount++; 
			var div = document.getElementById(divId[j]);
			div.innerHTML = array[j];  
			var divAd = document.getElementById('ad_craigsnotifica');
			var x = div.getElementsByTagName("script"); var len = x.length; var str = '';
			for(var i=0;i<len;i++) {
				var script = x[0];
				script.parentNode.removeChild(script);
				
				if (script.src != '') { 
					var js = document.createElement("script");
					//document.createElementNS('http://www.w3.org/1999/xhtml','html:script');
					js.type = "text/javascript";
					js.onload = jsIncluded;
					js.src = script.src;
					divAd.appendChild(js);
				} 
				else { 
					if (i == len-1) toBeEval = script.text; 
					else eval(script.text); 
				}
			}
		}  //end of for
	} catch (e) { 
		//"console.error(e); 
		div.innerHTML = "Ad: " + e; 
	}
}