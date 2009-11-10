function hideBusysign() {
  document.getElementById('bysy_indicator').style.display ='none';
}

function showBusysign() {
    showBusysign('Loading ...');
}

function showBusysign(message) {
    var node = document.createTextNode(message);
    var busy = document.getElementById('bysy_indicator');
    busy.replaceChild(node, busy.firstChild);
    busy.style.display ='inline';
}

function clearNode(id) {
    var feedback = document.getElementById(id);
    while(feedback.hasChildNodes()==true){ 
        feedback.removeChild(feedback.firstChild);
    }
}

function hide(id, linkId, hideText, showText) {
   var elementPanel = document.getElementById(id);
   var linkText;
   for (i = 0; i < elementPanel.childNodes.length; i++) {
        if (elementPanel.childNodes[i].className == "body") {
            if (elementPanel.childNodes[i].style.display =='none') {
                elementPanel.childNodes[i].style.display='inline';
                linkText = document.createTextNode(showText);
            } else {
                elementPanel.childNodes[i].style.display='none';
                linkText = document.createTextNode(hideText);
            }
        }      
    }
    // Case content is set inside a panel and got child <wicket:panel>
    if (linkText == null) {
        for (i = 0; i < elementPanel.childNodes.length; i++) {
            for (j = 0; j < elementPanel.childNodes[i].childNodes.length; j++) {
                if (elementPanel.childNodes[i].childNodes[j].className == "body") {
                    if (elementPanel.childNodes[i].childNodes[j].style.display =='none') {
                        elementPanel.childNodes[i].childNodes[j].style.display='inline';
                        linkText = document.createTextNode(showText);
                    } else {
                        elementPanel.childNodes[i].childNodes[j].style.display='none';
                        linkText = document.createTextNode(hideText);
                    }
                }     
            } 
        }
    }
    var link = document.getElementById(linkId);
    link.replaceChild(linkText, link.firstChild);
}