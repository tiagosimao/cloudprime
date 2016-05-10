function updateForm(){
    fetch("/api/config").then(function(response) {
        return response.json();
    }).then(function(config){
        printConfig(config);
    });
}

function updateJobs(){
    fetch("/api/job").then(function(response) {
        return response.json();
    }).then(function(jobs){
        printJobs(jobs);
    });
}

function updateNodes(){
    fetch("/api/node").then(function(response) {
        return response.json();
    }).then(function(nodes){
        printNodes(nodes);
    });
}

function printConfig(config) {
    var f = document.createElement("form");
    f.setAttribute('method',"post");
    f.setAttribute('action',"/api/config");
    
    Object.keys(config).forEach( k => { 
        var i = document.createElement("input");
        i.setAttribute('type',"text");
        i.setAttribute('value',config[k]);
        i.setAttribute('name', k);
        f.appendChild(i);
    });

    var s = document.createElement("input");
    s.setAttribute('type',"submit");
    s.setAttribute('value',"Submit");

    f.appendChild(s);

    document.getElementsByTagName('body')[0].appendChild(f);
}

function printJobs(jobs){
    var divs = d3.select("#jobs").selectAll("div").data(jobs).text(function (d){return d});
    divs.enter().append("div").text(function(d) { return d; });
    divs.exit().remove();
}

function printNodes(nodes){
    var divs = d3.select("#nodes").selectAll("div").data(nodes).text(function (d){return d});
    divs.enter().append("div").text(function(d) { return d; });
    divs.exit().remove();
}

window.setInterval(updateJobs, 1000);
window.setInterval(updateNodes, 1000);

updateForm();
