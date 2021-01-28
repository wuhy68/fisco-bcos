pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract Asset {
    // event
    event RegisterEvent(int256 ret, string account, int256 asset_value);
    event TransferEvent(int256 ret, string from_account, string to_account, int256 amount);
    event AddTransactionEvent(int256 ret, string id, string acc1, string acc2, int256 money);
    event UpdateTransactionEvent(int256 ret, string id, int256 money);
    event SplitTransactionEvent(int256 ret, string old_id, string new_id, string acc, int256 money);

    constructor() public {
        // 构造函数中创建t_asset表
        createTable();
    }

    function createTable() private {
        TableFactory tf = TableFactory(0x1001);
        // 资产管理表, key : account, field : asset_value
        // |   资产账户(主键)      |     信用额度       |
        // |-------------------- |-------------------|
        // |        account      |    asset_value    |
        // |---------------------|-------------------|
        //
        // 创建表
        tf.createTable("t_asset", "account", "asset_value");
        // 交易记录表, key: id, field: acc1, acc2, money, status
        // | 交易单号(key) | 债主 | 借债人 | 债务金额 |   状态   |
        // |-------------|------|-------|---------|---------|
        // |     id      | acc1 | acc2  |  money  | status  |
        // |-------------|------|-------|---------|---------|
        tf.createTable("t_transaction", "id","acc1, acc2, money, status");
    }

    // 返回t_asset
    function openAssetTable() private returns(Table) {
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_asset");
        return table;
    }

    // 返回t_transaction
    function openTransactionTable() private returns(Table) {
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_transaction");
        return table;
    }

    function select(string account) public constant returns(int256, int256) {
        // 打开表
        Table table = openAssetTable();
        // 查询
        Entries entries = table.select(account, table.newCondition());
        int256 asset_value = 0;
        if (0 == uint256(entries.size())) {
            return (-1, asset_value);
        } else {
            Entry entry = entries.get(0);
            return (0, int256(entry.getInt("asset_value")));
        }
    }

    function select_transaction(string id) public constant returns(int256[], bytes32[]) {
        // 打开表
        Table table = openTransactionTable();
        Entries entries = table.select(id, table.newCondition());

        int256[] memory int_list = new int256[](3);
        bytes32[] memory str_list = new bytes32[](2);
        if (0 == uint256(entries.size())) {
            int_list[0] = -1;
            return (int_list, str_list);
        } else {
            Entry entry = entries.get(0);
            int_list[1] = entry.getInt("money");
            int_list[2] = entry.getInt("status");
            str_list[0] = entry.getBytes32("acc1");
            str_list[1] = entry.getBytes32("acc2");
            return (int_list, str_list);
        }
    }

    function register(string account, int256 asset_value) public returns(int256){
        int256 ret_code = 0;
        int256 ret = 0;
        int256 temp_asset_value = 0;

        (ret, temp_asset_value) = select(account);
        if(ret != 0) {
            Table table = openAssetTable();
            
            Entry entry = table.newEntry();
            entry.set("account", account);
            entry.set("asset_value", int256(asset_value));

            int count = table.insert(account, entry);
            if (count == 1)
                ret_code = 0;
            else
                ret_code = -2;
        } else 
            ret_code = -1;

        emit RegisterEvent(ret_code, account, asset_value);

        return ret_code;
    }

    function addTransaction(string id, string acc1, string acc2, int256 money) public returns(int256){
        int256 ret_code = 0;
        int256 ret = 0;
        bytes32[] memory str_list = new bytes32[](2);
        int256[] memory int_list = new int256[](3);
        
        (int_list, str_list) = select_transaction(id);
        if(int_list[0] != int256(0)) {
            Table table = openTransactionTable();

            Entry entry0 = table.newEntry();
            entry0.set("id", id);
            entry0.set("acc1", acc1);
            entry0.set("acc2", acc2);
            entry0.set("money", int256(money));
            entry0.set("status", int256(money));

            int count = table.insert(id, entry0);
            if (count == 1) {
                ret = transfer(acc2,acc1,money);
                if(ret != 0) ret_code = -3;
                else ret_code = 0;
            } else ret_code = -2;
        } else ret_code = -1;

        emit AddTransactionEvent(ret_code, id, acc1, acc2, money);

        return ret_code;
    }

    function updateTransaction(string id, int256 money) public returns(int256, string[]){
        int256 ret_code = 0;
        bytes32[] memory str_list = new bytes32[](2);
        int256[] memory int_list = new int256[](3);
        string[] memory acc_list = new string[](2);
        (int_list, str_list) = select_transaction(id);
        acc_list[0] = byte32ToString(str_list[0]);
        acc_list[1] = byte32ToString(str_list[1]);

        if(int_list[0] == 0) {
            if(int_list[2] < money){
                ret_code = -2;
                emit UpdateTransactionEvent(ret_code, id, money);
                return (ret_code, acc_list);
            }

            Table table = openTransactionTable();

            Entry entry0 = table.newEntry();
            entry0.set("id", id);
            entry0.set("acc1", byte32ToString(str_list[0]));
            entry0.set("acc2", byte32ToString(str_list[1]));
            entry0.set("money", int_list[1]);
            entry0.set("status", (int_list[2] - money));

            int count = table.update(id, entry0, table.newCondition());
            if(count != 1) {
                ret_code = -3;
                emit UpdateTransactionEvent(ret_code, id, money);
                return (ret_code,acc_list);
            }

            int256 temp = transfer(byte32ToString(str_list[0]),byte32ToString(str_list[1]),money);
            if(temp != 0){
                ret_code = -4 * 10 + temp;
                emit UpdateTransactionEvent(ret_code, id, money);
                return (ret_code,acc_list);
            }

            ret_code = 0;
      
        } else ret_code = -1;

        emit UpdateTransactionEvent(ret_code, id, money);

        return (ret_code,acc_list);
    }

    function transfer(string from_account, string to_account, int256 amount) public returns(int256) {
        int ret_code = 0;
        int256 ret = 0;
        int256 from_asset_value = 0;
        int256 to_asset_value = 0;
        
        (ret, from_asset_value) = select(from_account);
        if(ret != 0) {
            ret_code = -1;
            emit TransferEvent(ret_code, from_account, to_account, amount);
            return ret_code;

        }

        (ret, to_asset_value) = select(to_account);
        if(ret != 0) {
            ret_code = -2;
            emit TransferEvent(ret_code, from_account, to_account, amount);
            return ret_code;
        }

        if(from_asset_value < amount) {
            ret_code = -3;
            emit TransferEvent(ret_code, from_account, to_account, amount);
            return ret_code;
        } 

        if (to_asset_value + amount < to_asset_value) {
            ret_code = -4;
            emit TransferEvent(ret_code, from_account, to_account, amount);
            return ret_code;
        }

        Table table = openAssetTable();

        Entry entry0 = table.newEntry();
        entry0.set("account", from_account);
        entry0.set("asset_value", int256(from_asset_value - amount));

        int count = table.update(from_account, entry0, table.newCondition());
        if(count != 1) {
            ret_code = -5;
            emit TransferEvent(ret_code, from_account, to_account, amount);
            return ret_code;
        }

        Entry entry1 = table.newEntry();
        entry1.set("account", to_account);
        entry1.set("asset_value", int256(to_asset_value + amount));

        table.update(to_account, entry1, table.newCondition());

        emit TransferEvent(ret_code, from_account, to_account, amount);

        return ret_code;
    }

    function splitTransaction(string old_id, string new_id, string acc, int256 money) public returns(int256) {
        int256 ret_code = 0;
        int256 ret = 0;
        int temp = 0;
        bytes32[] memory str_list = new bytes32[](2);
        int256[] memory int_list = new int256[](3);
        string[] memory acc_list = new string[](2);
        (int_list, str_list) = select_transaction(old_id);

        if(int_list[0] == 0) {
            (ret, temp) = select(acc);
            if(ret != 0) {
                ret_code = -5;
                emit SplitTransactionEvent(ret_code, old_id, new_id, acc, money);
                return ret_code;
            }

            if(int_list[2] < money){
                ret_code = -2;
                emit SplitTransactionEvent(ret_code, old_id, new_id, acc, money);
                return ret_code;
            }

            (ret,acc_list) = updateTransaction(old_id, money);
            if (ret != 0) {
                ret_code = -4;
                emit SplitTransactionEvent(ret_code, old_id, new_id, acc, money);
                return ret_code;
            }
            ret = addTransaction(new_id, acc, byte32ToString(str_list[1]), money);
            if (ret != 0) {
                ret_code = -3;
                emit SplitTransactionEvent(ret_code, old_id, new_id, acc, money);
                return ret_code;
            }

        } else ret_code = -1;


        emit SplitTransactionEvent(ret_code, old_id, new_id, acc, money);
        return ret_code;
    }

    function byte32ToString(bytes32 x) public constant returns (string) {
       
       bytes memory bytesString = new bytes(32);
        uint charCount = 0;
        for (uint j = 0; j < 32; j++) {
            byte char = byte(bytes32(uint(x) * 2 ** (8 * j)));
            if (char != 0) {
                bytesString[charCount] = char;
                charCount++;
            }
        }
        bytes memory bytesStringTrimmed = new bytes(charCount);
        for (j = 0; j < charCount; j++) {
            bytesStringTrimmed[j] = bytesString[j];
        }
        return string(bytesStringTrimmed);
   }
}