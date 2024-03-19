package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import play.api.libs.json._

case class Item(name: String, id: Int, price: Double, quantity: Int, icon: String)

object Item {
  implicit val itemWrites: Writes[Item] = new Writes[Item] {
    def writes(item: Item): JsValue = Json.obj(
      "nazwa_produktu" -> item.name,
      "id" -> item.id,
      "cena" -> item.price,
      "ilosc" -> item.quantity,
      "ikona" -> item.icon
    )
  }
}

@Singleton
class ProductsController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

    private var itemList: List[Item] = List(
        Item("apple", 1, 4.50, 5, "🍎"),
        Item("banana", 2, 49.99, 3, "🍌"),
        Item("pineapple", 3, 8.50, 10, "🍍")
    )

    def getAll() = Action { implicit request: Request[AnyContent] =>
        val products = Json.toJson(itemList)
        Ok(products)
    }

    def getOne(id: Int) = Action { implicit request: Request[AnyContent] =>
        val foundItemOption = itemList.find(_.id == id)
        val jsonItem = foundItemOption.map(Json.toJson(_)).getOrElse(Json.obj())
        Ok(jsonItem)
    }
    
    def addOne() = Action(parse.json) { implicit request: Request[JsValue] =>
        val itemNameOption = (request.body \ "name").asOpt[String]
        val itemIdOption = (request.body \ "id").asOpt[Int]
        val itemPriceOption = (request.body \ "price").asOpt[Double]
        val itemQuantityOption = (request.body \ "quantity").asOpt[Int]
        val itemIconOption = (request.body \ "icon").asOpt[String]

        if (itemNameOption.isEmpty || itemIdOption.isEmpty || itemPriceOption.isEmpty || itemQuantityOption.isEmpty) {
            BadRequest("Nie podano wszystkich wymaganych parametrow")
        } else {

            val itemName = itemNameOption.get
            val itemId = itemIdOption.get
            val itemPrice = itemPriceOption.get
            val itemQuantity = itemQuantityOption.get
            val itemIcon = itemIconOption.getOrElse("")

            val newItem = Item(itemName, itemId, itemPrice, itemQuantity, itemIcon)

            if (itemList.exists(_.id == itemId)) {
                BadRequest("Przedmiot o danym id juz istnieje")
            } else {
                itemList = newItem :: itemList
                Ok("Przedmiot dodany do listy")
            }
        }
    }

    def deleteOne(id: Int) = Action { implicit request: Request[AnyContent] =>
        val index = itemList.indexWhere(_.id == id)

        if (index != -1) {
            itemList = itemList.patch(index, Nil, 1)
            Ok("Przedmiot zostal usuniety")
        } else {
            NotFound("Nie znaleziono przedmiotu o zadanym id")
        }
    }

    def update(id: Int) = Action(parse.json) { implicit request: Request[JsValue] =>

        val index = itemList.indexWhere(_.id == id)

        if (index == -1) {
            NotFound("Nie znaleziono przedmiotu o zadanym id")
        } else {

            val itemNameOption = (request.body \ "name").asOpt[String]
            val itemPriceOption = (request.body \ "price").asOpt[Double]
            val itemQuantityOption = (request.body \ "quantity").asOpt[Int]
            val itemIconOption = (request.body \ "icon").asOpt[String]

            val updatedItem = itemList(index).copy(
                name = itemNameOption.getOrElse(itemList(index).name),
                price = itemPriceOption.getOrElse(itemList(index).price),
                quantity = itemQuantityOption.getOrElse(itemList(index).quantity),
                icon = itemIconOption.getOrElse(itemList(index).icon)
            )

            itemList = itemList.updated(index, updatedItem)
            Ok("Udalo sie zaktualizowac komponent")
        }
    }
}