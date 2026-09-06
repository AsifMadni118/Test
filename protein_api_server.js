const http = require('http');
const url = require('url');

const PORT = 3232;

const proteinData = [
  {
    "source": "Whey Protein Powder",
    "cost_grams": 1.23,
    "cost_package": "$42.74/tub",
    "protein_ per_pack": 696,
    "vegetarian": "T",
    "vegen": "F",
    "id": "bcc3"
  },
  {
    "source": "Chicken Breast",
    "cost_grams": 0.86,
    "cost_package": "$5.89/lb",
    "protein_ per_pack": 139,
    "vegetarian": "F",
    "vegen": "F",
    "id": "1813"
  },
  {
    "source": "Skim Milk",
    "cost_grams": 0.65,
    "cost_package": "$4.29/gal",
    "protein_ per_pack": 133,
    "vegetarian": "T",
    "vegen": "F",
    "id": "34fb"
  },
  {
    "source": "Whole Milk",
    "cost_grams": 0.85,
    "cost_package": "$5.19/gal",
    "protein_ per_pack": 123,
    "vegetarian": "T",
    "vegen": "F",
    "id": "aecd"
  },
  {
    "source": "Pork Tenderloin",
    "cost_grams": 1.49,
    "cost_package": "$8.59/22oz package",
    "protein_ per_pack": 115,
    "vegetarian": "F",
    "vegen": "F",
    "id": "d215"
  },
  {
    "source": "Peanuts",
    "cost_grams": 0.68,
    "cost_package": "$3.79/lb",
    "protein_ per_pack": 112,
    "vegetarian": "T",
    "vegen": "T",
    "id": "1cbd"
  },
  {
    "source": "93/7 beef (7% fat)",
    "cost_grams": 1.41,
    "cost_package": "$6.79/lb",
    "protein_ per_pack": 96,
    "vegetarian": "F",
    "vegen": "F",
    "id": "33ae"
  },
  {
    "source": "Ground Turkey",
    "cost_grams": 1.69,
    "cost_package": "$8.09/lb",
    "protein_ per_pack": 96,
    "vegetarian": "F",
    "vegen": "F",
    "id": "733c"
  },
  {
    "source": "Tilapia",
    "cost_grams": 1.65,
    "cost_package": "$7.49/lb",
    "protein_ per_pack": 91,
    "vegetarian": "F",
    "vegen": "F",
    "id": "b4ce"
  },
  {
    "source": "Salmon",
    "cost_grams": 2.35,
    "cost_package": "$10.69/lb",
    "protein_ per_pack": 91,
    "vegetarian": "F",
    "vegen": "F",
    "id": "aaa7"
  },
  {
    "source": "85/15 beef (15% fat)",
    "cost_grams": 1.38,
    "cost_package": "$5.79/lb",
    "protein_ per_pack": 84,
    "vegetarian": "F",
    "vegen": "F",
    "id": "4226"
  },
  {
    "source": "String Cheese",
    "cost_grams": 1.66,
    "cost_package": "$4.89/12 pack",
    "protein_ per_pack": 84,
    "vegetarian": "T",
    "vegen": "F",
    "id": "83f9"
  },
  {
    "source": "Greek Yogurt",
    "cost_grams": 1.5,
    "cost_package": "$5.99/32oz container",
    "protein_ per_pack": 80,
    "vegetarian": "T",
    "vegen": "F",
    "id": "9097"
  },
  {
    "source": "80/20 beef (20% fat)",
    "cost_grams": 1.39,
    "cost_package": "$5.29/lb",
    "protein_ per_pack": 76,
    "vegetarian": "F",
    "vegen": "F",
    "id": "5506"
  },
  {
    "source": "Cheddar Cheese",
    "cost_grams": 1.14,
    "cost_package": "$3.19/8oz block",
    "protein_ per_pack": 56,
    "vegetarian": "T",
    "vegen": "F",
    "id": "1820"
  },
  {
    "source": "Egg Whites in a carton",
    "cost_grams": 1.44,
    "cost_package": "$3.59/16oz carton",
    "protein_ per_pack": 50,
    "vegetarian": "T",
    "vegen": "F",
    "id": "c982"
  },
  {
    "source": "Chicken Legs (bone-in)",
    "cost_grams": 0.65,
    "cost_package": "$1.95/lb",
    "protein_ per_pack": 49,
    "vegetarian": "F",
    "vegen": "F",
    "Notes": "Price includes bones which are inedible. Chicken legs are generally more fatty as well.",
    "id": "fe70"
  },
  {
    "source": "Extra Firm Tofu",
    "cost_grams": 1.16,
    "cost_package": "$2.49/lb",
    "protein_ per_pack": 43,
    "vegetarian": "T",
    "vegen": "T",
    "id": "fa7a"
  },
  {
    "source": "Canned Black Beans",
    "cost_grams": 1.03,
    "cost_package": "$1.29/can",
    "protein_ per_pack": 25,
    "vegetarian": "T",
    "vegen": "T",
    "id": "e1c0"
  },
  {
    "source": "Tuna",
    "cost_grams": 0.95,
    "cost_package": "$1.09/can",
    "protein_ per_pack": 23,
    "vegetarian": "F",
    "vegen": "F",
    "id": "3175"
  },
  {
    "source": "Canned Chicken",
    "cost_grams": 3.54,
    "cost_package": "$3.19/4.5oz can",
    "protein_ per_pack": 18,
    "vegetarian": "F",
    "vegen": "F",
    "id": "2f62"
  },
  {
    "source": "Eggs (extra large)",
    "cost_grams": 1.12,
    "cost_package": "$4.69/dozen",
    "protein_ per_pack": 7,
    "vegetarian": "T",
    "vegen": "F",
    "Notes": "An extra large egg is estimated at 7 grams of protein per egg. A large egg has 6 grams of protein.",
    "id": "5ac6"
  }
];

const server = http.createServer((req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);

  if (parsedUrl.pathname === '/proteindata' && req.method === 'GET') {
    const query = parsedUrl.query;
    let results = [...proteinData];

    Object.keys(query).forEach((key) => {
      const val = query[key];
      if (val !== undefined && val !== '') {
        results = results.filter((item) => {
          if (item[key] === undefined) return true;
          return String(item[key]).toLowerCase() === String(val).toLowerCase();
        });
      }
    });

    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(results));
  } else {
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Not Found' }));
  }
});

server.listen(PORT, () => {
  console.log(`Standalone Protein API server running on http://localhost:${PORT}/proteindata`);
});
