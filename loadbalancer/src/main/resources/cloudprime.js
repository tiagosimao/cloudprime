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
        var d = document.createElement("div");
        var l = document.createElement("label"); 
        var i = document.createElement("input");
        
        l.textContent=k;
        
        i.setAttribute('type',"text");
        i.setAttribute('value',config[k]);
        i.setAttribute('name', k);
        
        l.appendChild(i);
        d.appendChild(l);
        f.appendChild(d);
    });

    var d = document.createElement("div");
    d.className="button";
    var s = document.createElement("button");
    s.setAttribute('type',"submit");
    s.setAttribute('value',"Submit");
    s.textContent = "Update";

    d.appendChild(s);
    f.appendChild(d);

    document.getElementsByTagName('body')[0].appendChild(f);
}

function jobToString(job) {
    var result = "<div class='job'>";
    result += "<b>Number:</b> " + job["number"] + "</br>";
    result += "<b>Cost:</b> " + job["cost"] + "</br>";
    return result + "</div>";
}

function nodeToString(d){
    var result = "<div class='node'>";
    result += "<b>Id:</b> " + d["id"] + "</br>";
    result += "<b>Address:</b> " + d["publicAddress"] + "</br>";
    result += "<b>Ready:</b> " + d["ready"] + "</br>";
    return result + "</div>";
}

function printJobs(jobs){
    var divs = d3.select("#jobs").selectAll("div").classed("job",true).data(jobs).html(function (d){
        return jobToString(d);
    });
    divs.enter().append("div").html(function(d) { return jobToString(d); });
    // divs.exit().remove();
}

function printNodes(nodes){
    var divs = d3.select("#nodes").selectAll("div").classed("node",true).data(nodes).html(function (d){
        return nodeToString(d);
    });
    divs.enter().append("div").html(function(d) { return nodeToString(d); });
    // divs.exit().remove();
}

window.setInterval(updateJobs, 1000);
window.setInterval(updateNodes, 1000);

updateForm();
