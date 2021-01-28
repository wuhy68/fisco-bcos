package org.fisco.bcos.asset.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ShaTest extends Contract {
    public static final String[] BINARY_ARRAY = {"60806040526040805190810160405280600e81526020017f48656c6c6f2c20536861546573740000000000000000000000000000000000008152506000908051906020019061004f9291906100a5565b5034801561005c57600080fd5b50615006600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061014a565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e657805160ff1916838001178555610114565b82800160010185558215610114579182015b828111156101135782518255916020019190600101906100f8565b5b5090506101219190610125565b5090565b61014791905b8082111561014357600081600090555060010161012b565b5090565b90565b610512806101596000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633bc5de301461005c5780638b0537581461008757806393730bbe146100c4575b600080fd5b34801561006857600080fd5b50610071610101565b60405161007e91906103eb565b60405180910390f35b34801561009357600080fd5b506100ae60048036036100a9919081019061034a565b6101a3565b6040516100bb91906103d0565b60405180910390f35b3480156100d057600080fd5b506100eb60048036036100e6919081019061034a565b61024b565b6040516100f891906103d0565b60405180910390f35b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156101995780601f1061016e57610100808354040283529160200191610199565b820191906000526020600020905b81548152906001019060200180831161017c57829003601f168201915b5050505050905090565b60006002826040518082805190602001908083835b6020831015156101dd57805182526020820191506020810190506020830392506101b8565b6001836020036101000a0380198251168184511680821785525050505050509050019150506020604051808303816000865af1158015610221573d6000803e3d6000fd5b5050506040513d601f19601f820116820180604052506102449190810190610321565b9050919050565b6000816040518082805190602001908083835b602083101515610283578051825260208201915060208101905060208303925061025e565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390209050919050565b60006102c3825161047b565b905092915050565b600082601f83011215156102de57600080fd5b81356102f16102ec8261043a565b61040d565b9150808252602083016020830185838301111561030d57600080fd5b610318838284610485565b50505092915050565b60006020828403121561033357600080fd5b6000610341848285016102b7565b91505092915050565b60006020828403121561035c57600080fd5b600082013567ffffffffffffffff81111561037657600080fd5b610382848285016102cb565b91505092915050565b61039481610471565b82525050565b60006103a582610466565b8084526103b9816020860160208601610494565b6103c2816104c7565b602085010191505092915050565b60006020820190506103e5600083018461038b565b92915050565b60006020820190508181036000830152610405818461039a565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561043057600080fd5b8060405250919050565b600067ffffffffffffffff82111561045157600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b6000819050919050565b6000819050919050565b82818337600083830152505050565b60005b838110156104b2578082015181840152602081019050610497565b838111156104c1576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a7230582087ff089b7df012cc2f19a20b5d243761f6747b1b91f569a1d363f93568dd52566c6578706572696d656e74616cf50037"};

    public static final String BINARY = String.join("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"60806040526040805190810160405280600e81526020017f48656c6c6f2c20536861546573740000000000000000000000000000000000008152506000908051906020019061004f9291906100a5565b5034801561005c57600080fd5b50615006600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061014a565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e657805160ff1916838001178555610114565b82800160010185558215610114579182015b828111156101135782518255916020019190600101906100f8565b5b5090506101219190610125565b5090565b61014791905b8082111561014357600081600090555060010161012b565b5090565b90565b610512806101596000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063a935d2841461005c578063d81443b414610099578063e211e0c1146100d6575b600080fd5b34801561006857600080fd5b50610083600480360361007e919081019061034a565b610101565b60405161009091906103d0565b60405180910390f35b3480156100a557600080fd5b506100c060048036036100bb919081019061034a565b6101a9565b6040516100cd91906103d0565b60405180910390f35b3480156100e257600080fd5b506100eb610215565b6040516100f891906103eb565b60405180910390f35b60006002826040518082805190602001908083835b60208310151561013b5780518252602082019150602081019050602083039250610116565b6001836020036101000a0380198251168184511680821785525050505050509050019150506020604051808303816000865af115801561017f573d6000803e3d6000fd5b5050506040513d601f19601f820116820180604052506101a29190810190610321565b9050919050565b6000816040518082805190602001908083835b6020831015156101e157805182526020820191506020810190506020830392506101bc565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390209050919050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102ad5780601f10610282576101008083540402835291602001916102ad565b820191906000526020600020905b81548152906001019060200180831161029057829003601f168201915b5050505050905090565b60006102c3825161047b565b905092915050565b600082601f83011215156102de57600080fd5b81356102f16102ec8261043a565b61040d565b9150808252602083016020830185838301111561030d57600080fd5b610318838284610485565b50505092915050565b60006020828403121561033357600080fd5b6000610341848285016102b7565b91505092915050565b60006020828403121561035c57600080fd5b600082013567ffffffffffffffff81111561037657600080fd5b610382848285016102cb565b91505092915050565b61039481610471565b82525050565b60006103a582610466565b8084526103b9816020860160208601610494565b6103c2816104c7565b602085010191505092915050565b60006020820190506103e5600083018461038b565b92915050565b60006020820190508181036000830152610405818461039a565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561043057600080fd5b8060405250919050565b600067ffffffffffffffff82111561045157600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b6000819050919050565b6000819050919050565b82818337600083830152505050565b60005b838110156104b2578082015181840152602081019050610497565b838111156104c1576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a72305820014b257bfa5dd37ca671d7f6ef590bccc9e97c66d0274773c478a5d2442a32116c6578706572696d656e74616cf50037"};

    public static final String SM_BINARY = String.join("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":true,\"inputs\":[],\"name\":\"getData\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_memory\",\"type\":\"bytes\"}],\"name\":\"getSha256\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_memory\",\"type\":\"bytes\"}],\"name\":\"getKeccak256\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]"};

    public static final String ABI = String.join("", ABI_ARRAY);

    public static final String FUNC_GETDATA = "getData";

    public static final String FUNC_GETSHA256 = "getSha256";

    public static final String FUNC_GETKECCAK256 = "getKeccak256";

    protected ShaTest(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public byte[] getData() throws ContractException {
        final Function function = new Function(FUNC_GETDATA, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeCallWithSingleValueReturn(function, byte[].class);
    }

    public TransactionReceipt getSha256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETSHA256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void getSha256(byte[] _memory, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETSHA256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetSha256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETSHA256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getGetSha256Input(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_GETSHA256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public Tuple1<byte[]> getGetSha256Output(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETSHA256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public TransactionReceipt getKeccak256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void getKeccak256(byte[] _memory, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetKeccak256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getGetKeccak256Input(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_GETKECCAK256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public Tuple1<byte[]> getGetKeccak256Output(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETKECCAK256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public static ShaTest load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new ShaTest(contractAddress, client, credential);
    }

    public static ShaTest deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(ShaTest.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }
}
