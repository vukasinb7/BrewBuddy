import 'package:BrewBuddy/models/Beer.dart';
import 'package:BrewBuddy/models/Brewery.dart';
import 'package:dropdown_search/dropdown_search.dart';
import 'package:flutter/material.dart';

class SearchPage extends StatefulWidget {
  const SearchPage({super.key});

  @override
  SearchPageState createState() => SearchPageState();
}

class SearchPageState extends State<SearchPage> {
  List<Beer> beers = [];
  List<Brewery> breweries = [];
  List<String> types = [];
  List<String> alcoholLevels = [];

  Brewery? selectedBrewery;
  String? selectedType;
  String? selectedLevel;

  List<Beer> filteredBeers = [];
  String query = '';
  TextEditingController _controller = TextEditingController();
  bool showFilters = false;

  @override
  void initState() {
    super.initState();
    fetchData();
  }

  void fetchData() {
    setState(() {
      beers = Beer.getBeers();
      breweries = Brewery.getBreweries();
      types = ["ALE", "IPA", "PILSNER"];
      alcoholLevels = ["LOW", "MEDIUM", "HIGH"];
      filteredBeers = beers;
    });
  }

  void updateSearchQuery(String newQuery) {
    setState(() {
      query = newQuery;
      filteredBeers = beers
          .where(
              (beer) => beer.name.toLowerCase().contains(query.toLowerCase()))
          .toList();
    });
  }

  void clearSearchQuery() {
    setState(() {
      query = '';
      _controller.clear();
      filteredBeers = beers;
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.onPrimary,
        title: const Text("BrewBuddy"),
        titleTextStyle: TextStyle(
          fontSize: 25,
          fontWeight: FontWeight.w700,
          color: Theme.of(context).colorScheme.onSecondary,
        ),
        centerTitle: true,
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: TextField(
              controller: _controller,
              decoration: InputDecoration(
                labelText: 'Search for a beer',
                hintText: 'Type the beer name',
                border: OutlineInputBorder(),
                suffixIcon: query.isNotEmpty
                    ? IconButton(
                  icon: Icon(Icons.clear),
                  onPressed: clearSearchQuery,
                )
                    : null,
              ),
              onChanged: updateSearchQuery,
            ),
          ),
          ElevatedButton(
            onPressed: () {
              setState(() {
                showFilters = !showFilters;
              });
            },
            child: Text(showFilters ? 'Hide Filters' : 'Show Filters'),
          ),
          if (showFilters)
            Expanded(
              child: ListView(
                children: [
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    child: DropdownSearch<Brewery>(
                      clearButtonProps: const ClearButtonProps(isVisible: true),
                      popupProps: PopupProps.menu(
                        showSearchBox: true,
                        searchFieldProps: const TextFieldProps(
                          decoration: InputDecoration(
                            labelText: 'Search for a brewery',
                            hintText: 'Type the brewery name',
                          ),
                        ),
                        itemBuilder: (context, brewery, isSelected) {
                          return ListTile(
                            title: Text(brewery.name),
                          );
                        },
                      ),
                      asyncItems: (String filter) => Future.value(breweries).then(
                              (breweries) => breweries
                              .where((brewery) => brewery.name.contains(filter))
                              .toList()),
                      itemAsString: (Brewery b) => b.name,
                      dropdownDecoratorProps: const DropDownDecoratorProps(
                        dropdownSearchDecoration: InputDecoration(
                          labelText: "Select a brewery",
                        ),
                      ),
                      onChanged: (Brewery? selectedBrewery) {
                        setState(() {
                          this.selectedBrewery = selectedBrewery;
                        });
                      },
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    child: DropdownSearch<String>(
                      clearButtonProps: const ClearButtonProps(isVisible: true),
                      popupProps: PopupProps.menu(
                        showSearchBox: true,
                        searchFieldProps: const TextFieldProps(
                          decoration: InputDecoration(
                            labelText: 'Search for a type',
                            hintText: 'Type the beer type',
                          ),
                        ),
                        itemBuilder: (context, item, isSelected) {
                          return ListTile(
                            title: Text(item),
                          );
                        },
                      ),
                      asyncItems: (String filter) => Future.value(types).then(
                              (types) => types
                              .where((type) => type.contains(filter))
                              .toList()),
                      itemAsString: (String t) => t,
                      dropdownDecoratorProps: const DropDownDecoratorProps(
                        dropdownSearchDecoration: InputDecoration(
                          labelText: "Select a type",
                        ),
                      ),
                      onChanged: (String? selectedType) {
                        setState(() {
                          this.selectedType = selectedType;
                        });
                      },
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    child: DropdownSearch<String>(
                      clearButtonProps: const ClearButtonProps(isVisible: true),
                      popupProps: PopupProps.menu(
                        showSearchBox: true,
                        searchFieldProps: const TextFieldProps(
                          decoration: InputDecoration(
                            labelText: 'Search for an alcohol level',
                            hintText: 'Type the alcohol level',
                          ),
                        ),
                        itemBuilder: (context, item, isSelected) {
                          return ListTile(
                            title: Text(item),
                          );
                        },
                      ),
                      asyncItems: (String filter) => Future.value(alcoholLevels)
                          .then((levels) => levels
                          .where((level) => level.contains(filter))
                          .toList()),
                      itemAsString: (String t) => t,
                      dropdownDecoratorProps: const DropDownDecoratorProps(
                        dropdownSearchDecoration: InputDecoration(
                          labelText: "Select an alcohol level",
                        ),
                      ),
                      onChanged: (String? selectedLevel) {
                        setState(() {
                          this.selectedLevel = selectedLevel;
                        });
                      },
                    ),
                  )
                ],
              ),
            ),
            Expanded(
              child: ListView.builder(
                itemCount: filteredBeers.length,
                itemBuilder: (context, index) {
                  return ListTile(
                    title: Text(filteredBeers[index].name),
                    onTap: () {
                      // Handle the beer selection
                      print('Selected beer: ${filteredBeers[index].name}');
                    },
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}
