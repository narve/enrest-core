const propValAttrs = [
    'content',
    'value',
    'datetime',
    'src',
];

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

function findDescendants(element, matcher, list = []) {
    for (let child of element.children) {
        if (matcher(child)) {
            list.push(child);
        } else {
            findDescendants(child, matcher, list);
        }
    }
    return list;
}

function toObject(e) {
    if (!e.hasAttribute('itemscope')) {
        throw 'no itemscope for ' + e.tagName + " " + Array.from(e.attributes).map(a => a.name + "=" + a.value).join(",");
    }

    let itemType;
    if (e.hasAttribute('itemtype')) {
        itemType = e.attributes['itemtype'].value;
    } else if (e.tagName === 'A') {
        itemType = 'link';
    } else {
        itemType = 'string';
        // throw 'No item type';
    }

    log(`Processing ${e.tagName} of type ${itemType}`);

    const isArray = itemType.indexOf('List') >= 0;
    if (itemType === 'string') {
        return e.textContent;
    } else if (itemType === 'link') {
        return {
            href: e.href,
            rel: e.rel,
            clz: e.className,
            text: e.textContent.trim(),
        };
    } else if (isArray) {
        const items = findDescendants(e, c => c.hasAttribute('itemscope'));
        log(` => Produced an array of type ${itemType}: `, items);
        const res = [];
        res.type = e.attributes['itemtype'].value;
        for (let element of items) {
            res.push(toAny(element));
        }
        return res;
    } else {
        const itemPropElements = findDescendants(e, c => c.hasAttribute('itemprop'));

        let propNames = itemPropElements.map(e => e.attributes['itemprop'].value).join("; ");

        const object = {
            type: itemType,
        };
        for (let child of itemPropElements) {
            object[child.attributes['itemprop'].value] = toAny(child);
        }
        log(` => Produced an object of type ${itemType}: `, {propNames, object});
        return object;
    }
}

function toAny(e) {
    let actual;
    if (e.hasAttribute('itemscope')) {
        log('toAny: Found itemscope, type=', e.attributes['itemtype']?.value);
        actual = e;
    } else if (e.children.length === 1 && e.children[0].hasAttribute('itemscope')) {
        actual = e.children[0];
        log('toAny: Found itemscope in child, type=', actual.attributes['itemtype']?.value);
    } else {
        const desc = findDescendants(e, x => x.hasAttribute('itemscope'));
        if (desc.length === 1) {
            log(`toAny: Found ${desc.length} descendant(s), assuming simple prop: `, e);
            return toAny(desc[0]);
        }
        log('toAny: Found nothing, assuming simple prop: ', e);
        actual = e;
    }
    return actual.hasAttribute('itemscope') ? toObject(actual) : toString(actual);
}


function parseHtml(s) {
    const document = new DOMParser().parseFromString(s, 'text/html');
    // TODO: Mark top-level api element with something. 
    // const dataElements = Array.from(document.querySelectorAll("[itemtype='http://dv8.no/SearchResult']"));
    const dataElements = findDescendants(document, e => e.hasAttribute('itemscope'));
    const dataObjects = dataElements.map(e => toObject(e));
    log(`Done parsing ${dataObjects.length} object(s): `, dataObjects);
    return dataObjects;
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
        .then(o => showObject2(o))
        .catch(e => log('Failed: ' + e, e))
        .finally(() => log('done fetching'));
}


document.addEventListener("DOMContentLoaded", () => init());