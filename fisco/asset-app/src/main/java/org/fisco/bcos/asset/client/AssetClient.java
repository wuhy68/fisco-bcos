package org.fisco.bcos.asset.client;

import java.math.BigInteger;
import java.util.*;
import java.io.*;
import java.util.List;
import java.util.Properties;
import org.fisco.bcos.asset.contract.Asset;
import org.fisco.bcos.asset.contract.Asset.RegisterEventEventResponse;
import org.fisco.bcos.asset.contract.Asset.TransferEventEventResponse;
import org.fisco.bcos.asset.contract.Asset.AddTransactionEventEventResponse;
import org.fisco.bcos.asset.contract.Asset.UpdateTransactionEventEventResponse;
import org.fisco.bcos.asset.contract.Asset.SplitTransactionEventEventResponse;
import org.fisco.bcos.asset.client.CLI;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class AssetClient {

  static Logger logger = LoggerFactory.getLogger(AssetClient.class);

  private BcosSDK bcosSDK;
  private Client client;
  private CryptoKeyPair cryptoKeyPair;

  public void initialize() throws Exception {
    @SuppressWarnings("resource")
    ApplicationContext context =
        new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
    bcosSDK = context.getBean(BcosSDK.class);
    client = bcosSDK.getClient(1);
    cryptoKeyPair = client.getCryptoSuite().createKeyPair();
    client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
    logger.debug("create client for group1, account address is " + cryptoKeyPair.getAddress());
  }

  public void deployAssetAndRecordAddr() {

    try {
      Asset asset = Asset.deploy(client, cryptoKeyPair);
      System.out.println(
          " deploy Asset success, contract address is " + asset.getContractAddress());

      recordAssetAddr(asset.getContractAddress());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
      System.out.println(" deploy Asset contract failed, error message is  " + e.getMessage());
    }
  }

  public void recordAssetAddr(String address) throws FileNotFoundException, IOException {
    Properties prop = new Properties();
    prop.setProperty("address", address);
    final Resource contractResource = new ClassPathResource("contract.properties");
    FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
    prop.store(fileOutputStream, "contract address");
  }

  public String loadAssetAddr() throws Exception {
    // load Asset contact address from contract.properties
    Properties prop = new Properties();
    final Resource contractResource = new ClassPathResource("contract.properties");
    prop.load(contractResource.getInputStream());

    String contractAddress = prop.getProperty("address");
    if (contractAddress == null || contractAddress.trim().equals("")) {
      throw new Exception(" load Asset contract address failed, please deploy it first. ");
    }
    logger.info(" load Asset address from contract.properties, address is {}", contractAddress);
    return contractAddress;
  }

	public boolean queryAssetAmount(String assetAccount) {
		try {
			String contractAddress = loadAssetAddr();

			Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
			Tuple2<BigInteger, BigInteger> result = asset.select(assetAccount);
			if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf(" Your credit limit is: %s \n", result.getValue2());
				return true;
			} else {
				System.out.printf(" %s asset account is not exist \n", assetAccount);
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" queryAssetAmount exception, error message is {}", e.getMessage());

			System.out.printf(" query asset account failed, error message is %s\n", e.getMessage());
		}
		return false;
	}

  public void queryAssetTransaction(String t_id) {
		try {
			String contractAddress = loadAssetAddr();
			Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
			Tuple2<List<BigInteger>, List<byte[]>> result = asset.select_transaction(t_id);
			if (result.getValue1().get(0).compareTo(new BigInteger("0")) == 0) {
				String temp1 = new String(result.getValue2().get(0));
				String temp2 = new String(result.getValue2().get(1));
				System.out.printf("Transaction\n ID: " + t_id + "; Acc1: " + temp1 + "; Acc2: " + temp2 + "; Money: " + result.getValue1().get(1) + "; Current: " + result.getValue1().get(2) + "\n");
			} else {
				System.out.printf("Transaction %s is not exist \n", t_id);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" queryAssetAmount exception, error message is {}", e.getMessage());

			System.out.printf(" query asset account failed, error message is %s\n", e.getMessage());
		}
	}

  public void registerAssetAccount(String assetAccount, BigInteger amount) {
    try {
      String contractAddress = loadAssetAddr();

      Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
      TransactionReceipt receipt = asset.register(assetAccount, amount);
      List<Asset.RegisterEventEventResponse> response = asset.getRegisterEventEvents(receipt);
      if (!response.isEmpty()) {
        if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
          System.out.printf(
              " register asset account success => asset: %s, value: %s \n", assetAccount, amount);
        } else {
          System.out.printf(
              " register asset account failed, ret code is %s \n", response.get(0).ret.toString());
        }
      } else {
        System.out.println(" event log not found, maybe transaction not exec. ");
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();

      logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
      System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
    }
  }
  
  public void addAssetTransaction(String t_id, String acc1, String acc2, BigInteger money) {
		try {
			String contractAddress = loadAssetAddr();

			Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
			TransactionReceipt receipt = asset.addTransaction(t_id, acc1, acc2, money);
			List<AddTransactionEventEventResponse> response = asset.getAddTransactionEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" Add transaction success! id:" + t_id+"\n");
				} else {
					System.out.printf(" Add transaction failed, ret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
  }
  
  public void updateAssetTransaction(String t_id, BigInteger money) {
		try {
			String contractAddress = loadAssetAddr();
			Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
			TransactionReceipt receipt = asset.updateTransaction(t_id, money);
			List<UpdateTransactionEventEventResponse> response = asset.getUpdateTransactionEventEvents(receipt);
			// Tuple2<BigInteger, List<String>> result = asset.getUpdateTransactionOutput(asset.updateTransaction(t_id, money).send());
			
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" Update transaction success.\n" );
				} else {
					System.out.printf(" Update transaction failed, ret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}

  public void transferAsset(String fromAssetAccount, String toAssetAccount, BigInteger amount) {
    try {
      String contractAddress = loadAssetAddr();
      Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
      TransactionReceipt receipt = asset.transfer(fromAssetAccount, toAssetAccount, amount);
      List<Asset.TransferEventEventResponse> response = asset.getTransferEventEvents(receipt);
      if (!response.isEmpty()) {
        if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
          System.out.printf(
              " transfer success => from_asset: %s, to_asset: %s, amount: %s \n",
              fromAssetAccount, toAssetAccount, amount);
        } else {
          System.out.printf(
              " transfer asset account failed, ret code is %s \n", response.get(0).ret.toString());
        }
      } else {
        System.out.println(" event log not found, maybe transaction not exec. ");
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();

      logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
      System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
    }
  }

  public void splitAssetTransaction(String old_id, String new_id, String acc, BigInteger money) {
		try {
			String contractAddress = loadAssetAddr();
			Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
			TransactionReceipt receipt = asset.splitTransaction(old_id, new_id, acc, money);
			List<SplitTransactionEventEventResponse> response = asset.getSplitTransactionEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" Split transaction success! old_id: "+ old_id +" new_id: "+new_id+"\n");
				} else {
					System.out.printf(" Split transaction failed, ret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}

  public static void Usage() {
    System.out.println(" Usage:");
    System.out.println(
        "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient deploy");
    System.out.println(
        "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient query account");
    System.out.println(
        "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient register account value");
    System.out.println(
        "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient transfer from_account to_account amount");
    System.exit(0);
  }

  public static void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in);
		CLI platform = new CLI();
		

		AssetClient client = new AssetClient();
		client.initialize();
		client.deployAssetAndRecordAddr();
		int transaction_id = 1000000;

		while (platform.getStatus()) {
      while (platform.getStatus() == true && platform.login() == false);
      
      if (!platform.getStatus()) break;
      
			String x;
      Map<String,String> map = platform.getMap();
      
			for (String key : map.keySet()) {
				if (client.queryAssetAmount(key) == false) {
					if (key.compareTo("bank") != 0) {
						if (key.compareTo("user") == 0)
							x = "10000";
						else
						  x = "1000";
					} else
            x = "1000000";
            
					client.registerAssetAccount(key,new BigInteger(x));
				}
			}

			platform.clearFile();
      boolean judge = true;
      
      while(judge){
        platform.msg();
        int choice, int1, int2;
        String str1,str2,str3, str4;

        if(scanner.hasNextInt()){
          choice = scanner.nextInt();
          switch(choice){
            case 0:
              platform.setCurrentNull();
              judge = false;
              
              break;
            case 1:
              System.out.println("------Check my credit------\n");
              client.queryAssetAmount(platform.getCurrent());
              System.out.print("Wait for key...");
              str4 = (String)scanner.nextLine();

              break;
            case 2:
              System.out.println("------Trade with others------\n");
              str1 = (String)scanner.nextLine();
              System.out.print("Trader Account: ");
              str1 = (String)scanner.nextLine();
              System.out.print("Transaction Amount: ");
              int1 = scanner.nextInt();
              str3 = int1+"";
              str2 = transaction_id +"";
              transaction_id += 1;
              client.addAssetTransaction(str2,str1, platform.getCurrent() ,new BigInteger(str3));
              System.out.print("Wait for key...");
              str4 = (String)scanner.nextLine();

              break;

            case 3:
              System.out.println("------Financing/Loan------\n");
              str1 = (String)scanner.nextLine();
              System.out.print("Financing/Loan Amount: ");
              int1 = scanner.nextInt();
              str1 = transaction_id +"";
              transaction_id += 1;
              str2 = int1+"";
              client.addAssetTransaction(str1, "bank", platform.getCurrent(),new BigInteger(str2));
              System.out.print("Wait for key...");
              str4 = (String)scanner.nextLine();

              break;

            case 4:
              System.out.println("------IOU Spilt------\n");
              str1 = (String)scanner.nextLine();
              System.out.print("Beneficiary: ");
              str1 = (String)scanner.nextLine();
              System.out.print("Original ID: ");
              str2 = (String)scanner.nextLine();
              System.out.print("Transfer amount: ");
              int1 = scanner.nextInt();
              str3 = transaction_id+"";
              transaction_id+=1;
              str4 = int1 + "";
              client.splitAssetTransaction(str2,str3,str1,new BigInteger(str4));
              System.out.print("Wait for key...");
              str4 = (String)scanner.nextLine();
              break;
            
            case 5:
              System.out.println("------Transfer/Loan Repayment------\n");
              str1 = (String)scanner.nextLine();
              System.out.print("Trade ID: ");
              str1 = (String)scanner.nextLine();
              System.out.print("Transfer Amount: ");
              int1 = scanner.nextInt();
              str2 = transaction_id +"";
              transaction_id += 1;
              str3 = int1+"";
              client.updateAssetTransaction( str1, new BigInteger(str3));
              System.out.print("Wait for key...");
              str4 = (String)scanner.nextLine();

              break;
            
            case 6:
              System.out.print("------Trade Check------\n");
              str1 = (String)scanner.nextLine();
              System.out.print("Original ID: ");
              str1 = (String)scanner.nextLine();
              client.queryAssetTransaction(str1);
              System.out.print("Wait for key...");
              str4 = (String)scanner.nextLine();
            
            default:
              System.out.print("Invalid input! Wait for key...");
              str1 = (String)scanner.nextLine();
              break;
          }
        }
      }
    }
    
		platform.clearFile();
    platform.writeToFile();
    System.out.println("Trade Finish");
    System.exit(0);
  }
}
