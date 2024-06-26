import 'package:flutter/material.dart';


class ItemListPage extends StatefulWidget {
  final List<Widget> widgets;

  const ItemListPage({required this.widgets, super.key});

  @override
  ItemListPageState createState() => ItemListPageState();
}

class ItemListPageState extends State<ItemListPage> {
  List<Widget> widgets = [];

  @override
  void initState() {
    super.initState();
    widgets = widget.widgets;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        body: Padding(
          padding: const EdgeInsets.all(10.0),
          child: GridView.builder(
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                crossAxisSpacing: 20,
                mainAxisSpacing: 20,
                childAspectRatio: 0.681,

              ),


              itemCount: widgets.length,
              itemBuilder: (context, index) {
                return widgets[index];
              },
            ),
        ),
        );
  }
}
