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