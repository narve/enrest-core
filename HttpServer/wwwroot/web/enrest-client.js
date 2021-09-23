const propValAttrs = [
    'content',
    'value',
    'datetime',
    'src',
];

const itemprop = 'itemprop';
const itemtype = 'itemtype';
const itemscope = 'itemscope';

const attrIsPresent = attr => element => element.hasAttribute(attr);

function describeElement(element) {
    const attrs = [itemprop, itemscope, itemtype].map(a => a + "=" + element.attributes[a]?.value).join(",");
    return `${element.tagName} (${attrs})`;
}


function toString(e) {
    if (e.hasAttribute('content'))
        throw 'content: ' + e;
    for (const attr of propValAttrs) {
        if (e.hasAttribute(attr)) {
            log("attr: " + attr);
            return e.attributes[attr].value;
        }
    }
    // Hack: Not proper according to MicroData spec
    const contentHolder = e.querySelector('[content]');
    if (contentHolder)
        return contentHolder.attributes['content'].value;
    return e.textContent.trim();
}

function findDescendants(element, matcher, list = [], stopper) {
    for (let child of element.children) {
        if (matcher(child)) {
            list.push(child);
        } else if (stopper && stopper(child)) {
            // Don't recurse
        } else {
            findDescendants(child, matcher, list, stopper);
        }
    }
    return list;
}

function toObject(element) {
    log(`toObject: processing ${describeElement(element)}`);

    const itemProps = findDescendants(element, attrIsPresent(itemprop));
    const object = {
        type: element.attributes[itemtype].value,
    };
    for (let child of itemProps) {
        object[child.attributes[itemprop].value] = toAny(child);
    }
    return object;
}


function toAny(element) {
    log('toAny: processing: ' + describeElement(element));
    const propItems = findDescendants(element, attrIsPresent(itemprop), [], attrIsPresent(itemscope));
    const arrayItems = findDescendants(element, attrIsPresent(itemscope), [], attrIsPresent(itemprop));

    if (arrayItems.length > 0 && propItems.length > 0) {
        log("  propItems: ", propItems.map(describeElement).join(";"));
        log("  arrayItems: ", arrayItems.map(describeElement).join(";"));
        throw "ArrayOrObject? Source=" + describeElement(element) + "; "
            // + "PropItems=" + propItems.map(describeElement).join(";") 
            // + " <|||> ArrayItems=" + arrayItems.map(describeElement).join(";")
            ;
    }

    if (arrayItems.length > 0) {
        return arrayItems.map(e => toAny(e));
    } else if (propItems.length > 0) {
        return toObject(element);
    } else {
        return toString(element);
    }
    //
    // let actual;
    // if (element.hasAttribute('itemscope')) {
    //     log('toAny: Found itemscope, type=', element.attributes['itemtype']?.value);
    //     actual = element;
    // } else if (element.children.length === 1 && element.children[0].hasAttribute('itemscope')) {
    //     actual = element.children[0];
    //     log('toAny: Found itemscope in child, type=', actual.attributes['itemtype']?.value);
    // } else {
    //     const desc = findDescendants(element, x => x.hasAttribute('itemscope'));
    //     if (desc.length === 1) {
    //         log(`toAny: Found ${desc.length} descendant(s), assuming simple prop: `, element);
    //         return toAny(desc[0]);
    //     }
    //     log('toAny: Found nothing, assuming simple prop: ', element);
    //     actual = element;
    // }
    // return actual.hasAttribute('itemscope') ? toObject(actual) : toString(actual);
}


function parseHtml(s) {
    const document = new DOMParser().parseFromString(s, 'text/html');
    return findDescendants(document, e => e.hasAttribute('itemscope'))
        .map(e => toAny(e));
}


function log(msg, toReturn) {
    if (undefined === toReturn)
        console.log(msg);
    else
        console.log(msg, toReturn);
    return toReturn;
}

function showObject(o) {
    document.getElementById("enrest-sink-html-json").textContent = JSON.stringify(o, null, 2);
    // log('shown, hopefully');
}

function showObject2(o) {
    document.getElementById("enrest-sink-json").textContent = JSON.stringify(o, null, 2);
    // log('shown, hopefully');
}

function showHtml(o) {
    document.getElementById("enrest-sink-html-source").textContent = o;
    document.getElementById("enrest-sink-html").innerHTML = o;
    // log('shown, hopefully');
    return o;
}

function init() {
    const params = new URLSearchParams(window.location.search);
    const url = params.get('path') || '/item';
    log('URL: ' + url);
    fetch(url, {
        headers: {
            "Accept": "text/html",
        }
    })
        // .then(r => log('Got response!', r))
        .then(r => r.text())
        // .then(t => log('Got text: ' + t, t))
        .then(t => showHtml(t))
        .then(t => parseHtml(t))
        .then(o => log('Got object:', o))
        .then(o => showObject(o))
        .then(() => fetch(url, {headers: {'accept': 'application/json'}}))
        .then(r => r.json())
        // .then(o => showObject2(o))
        .catch(e => log('Failed: ' + e, e))
        .finally(() => log('done fetching'));
}


document.addEventListener("DOMContentLoaded", () => init());