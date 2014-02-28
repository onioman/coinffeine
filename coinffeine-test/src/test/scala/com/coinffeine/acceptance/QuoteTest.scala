package com.coinffeine.acceptance

import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.ShouldMatchers

import com.coinffeine.common.currency.{BtcAmount, CurrencyCode}
import com.coinffeine.common.protocol.{Ask, Bid, Order, Quote}

class QuoteTest
  extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with Eventually with ShouldMatchers {

  var mockPay = new MockPay()
  var broker = new TestBrokerComponent().broker
  var peers = new TestPeerFactory(broker, mockPay)

  override def afterAll() {
    mockPay.shutdown()
    broker.shutdown()
  }

  feature("Query current price quote") {

    scenario("No previous bidding or asking") {
      peers.withPeer { peer =>
        peer.askForQuote(CurrencyCode.EUR)
        eventually {
          peer.lastQuote should be (Some(Quote.empty))
        }
      }
    }

    scenario("Previous bidding and asking") {
      peers.withPairOfPeers { (sam, bob) =>
        sam.placeOrder(Order(Bid, BtcAmount(0.02), EUR(300)))
        bob.placeOrder(Order(Ask, BtcAmount(0.5), EUR(400)))

        sam.askForQuote(CurrencyCode.EUR)
        eventually {
          peer.lastQuote should be (Some(Quote(EUR(300) -> EUR(400), lastPrice = None)))
        }
      }
    }
  }
}
