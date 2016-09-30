package simalatorInterface;

import java.util.ArrayList;
import java.util.HashMap;

import demo.acl.Product;
import demo.common.ProductDB;

public class SimplePlanner {

	private static int[][] heuristics = {
			//1  2  3  4  5  6  7  8  9
			{ 1, 3, 5, 8, 6, 4, 5, 7, 9 }, // 0 (0;0 start pos)
			{ 0, 2, 4, 7, 5, 3, 4, 6, 8 }, // 1
			{ 2, 0, 2, 5, 7, 5, 6, 8, 6 }, // 2
			{ 4, 2, 0, 3, 5, 7, 8, 6, 4 }, // 3
			{ 7, 5, 3, 0, 2, 4, 5, 3, 1 }, // 4
			{ 5, 7, 5, 2, 0, 2, 3, 1, 3 }, // 5
			{ 3, 5, 7, 4, 2, 0, 1, 3, 5 }, // 6
			{ 4, 6, 8, 5, 3, 1, 0, 2, 4 }, // 7
			{ 6, 8, 6, 3, 1, 3, 2, 0, 2 }, // 8
			{ 8, 6, 4, 1, 3, 5, 4, 2, 0 }, // 9
	};
/*
	private static int getMinIdFromId(int id) {
		int min = 1000;
		int minIdx = -1;
		for (int i = 0; i < heuristics[id].length; i++) {
			if (heuristics[id][i] < min) {
				min = heuristics[id][i];
				minIdx = i;
			}
		}
		return minIdx;
	}
*/
	public static ArrayList<Product> makeGreedyPickupPlan(ArrayList<Product> shoppingList, Product currentPos) {

		ArrayList<Product> ret = new ArrayList<>();
		int posID;
		if (currentPos == null) {
			posID = 0;
		} else {
			posID = Integer.parseInt(currentPos.getId().split("-")[1]);
		}
		while (!shoppingList.isEmpty()) {
			Product currentClosestProduct = null;
			int minH = 1000;
			for (Product p : shoppingList) {
				int id = Integer.parseInt(p.getId().split("-")[1]);
				if (heuristics[posID][id-1] < minH) {
					minH = heuristics[posID][id-1];
					currentClosestProduct = p;
				}
			}
			ret.add(currentClosestProduct);
			posID = Integer.parseInt(currentClosestProduct.getId().split("-")[1]);
			shoppingList.remove(currentClosestProduct);
		}

		return ret;
	}

	public static void main(String[] args) {
		HashMap<String, Product> products = new ProductDB().getDB();
		
		ArrayList<Product> testList = new ArrayList<>();
		
		testList.add(products.get("R5COP-9"));
		//testList.add(products.get("R5COP-6"));
		testList.add(products.get("R5COP-1"));
		
		for(Product p: testList){
			System.out.println(p.getId());
		}
		ArrayList<Product> orderedList = makeGreedyPickupPlan(testList, null);
		System.out.println("Plan:");
		for(Product p: orderedList){
			System.out.println(p.getId());
		}
		
	}
}
