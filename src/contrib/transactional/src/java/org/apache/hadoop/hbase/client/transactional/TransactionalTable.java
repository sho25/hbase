begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|transactional
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Delete
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Get
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|HConnection
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|HTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Put
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Result
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ResultScanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Scan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ScannerCallable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ServerCallable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|HBaseRPC
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|TransactionalRegionInterface
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Table with transactional support.  *   */
end_comment

begin_class
specifier|public
class|class
name|TransactionalTable
extends|extends
name|HTable
block|{
static|static
block|{
name|TransactionalRPC
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param conf    * @param tableName    * @throws IOException    */
specifier|public
name|TransactionalTable
parameter_list|(
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param conf    * @param tableName    * @throws IOException    */
specifier|public
name|TransactionalTable
parameter_list|(
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|TransactionalServerCallable
parameter_list|<
name|T
parameter_list|>
extends|extends
name|ServerCallable
argument_list|<
name|T
argument_list|>
block|{
specifier|protected
name|TransactionState
name|transactionState
decl_stmt|;
specifier|protected
name|TransactionalRegionInterface
name|getTransactionServer
parameter_list|()
block|{
return|return
operator|(
name|TransactionalRegionInterface
operator|)
name|server
return|;
block|}
specifier|protected
name|void
name|recordServer
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|transactionState
operator|.
name|addRegion
argument_list|(
name|location
argument_list|)
condition|)
block|{
name|getTransactionServer
argument_list|()
operator|.
name|beginTransaction
argument_list|(
name|transactionState
operator|.
name|getTransactionId
argument_list|()
argument_list|,
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * @param connection      * @param tableName      * @param row      * @param transactionState      */
specifier|public
name|TransactionalServerCallable
parameter_list|(
specifier|final
name|HConnection
name|connection
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|TransactionState
name|transactionState
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|transactionState
operator|=
name|transactionState
expr_stmt|;
block|}
block|}
comment|/**    * Method for getting data from a row    * @param get the Get to fetch    * @return the result    * @throws IOException    * @since 0.20.0    */
specifier|public
name|Result
name|get
parameter_list|(
specifier|final
name|TransactionState
name|transactionState
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionServerWithRetries
argument_list|(
operator|new
name|TransactionalServerCallable
argument_list|<
name|Result
argument_list|>
argument_list|(
name|super
operator|.
name|getConnection
argument_list|()
argument_list|,
name|super
operator|.
name|getTableName
argument_list|()
argument_list|,
name|get
operator|.
name|getRow
argument_list|()
argument_list|,
name|transactionState
argument_list|)
block|{
specifier|public
name|Result
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|recordServer
argument_list|()
expr_stmt|;
return|return
name|getTransactionServer
argument_list|()
operator|.
name|get
argument_list|(
name|transactionState
operator|.
name|getTransactionId
argument_list|()
argument_list|,
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|get
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    *     * @param delete     * @throws IOException    * @since 0.20.0    */
specifier|public
name|void
name|delete
parameter_list|(
specifier|final
name|TransactionState
name|transactionState
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionServerWithRetries
argument_list|(
operator|new
name|TransactionalServerCallable
argument_list|<
name|Object
argument_list|>
argument_list|(
name|super
operator|.
name|getConnection
argument_list|()
argument_list|,
name|super
operator|.
name|getTableName
argument_list|()
argument_list|,
name|delete
operator|.
name|getRow
argument_list|()
argument_list|,
name|transactionState
argument_list|)
block|{
specifier|public
name|Object
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|recordServer
argument_list|()
expr_stmt|;
name|getTransactionServer
argument_list|()
operator|.
name|delete
argument_list|(
name|transactionState
operator|.
name|getTransactionId
argument_list|()
argument_list|,
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|delete
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Commit a Put to the table.    *<p>    * If autoFlush is false, the update is buffered.    * @param put    * @throws IOException    * @since 0.20.0    */
specifier|public
specifier|synchronized
name|void
name|put
parameter_list|(
name|TransactionState
name|transactionState
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
comment|//super.validatePut(put);
name|super
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionServerWithRetries
argument_list|(
operator|new
name|TransactionalServerCallable
argument_list|<
name|Object
argument_list|>
argument_list|(
name|super
operator|.
name|getConnection
argument_list|()
argument_list|,
name|super
operator|.
name|getTableName
argument_list|()
argument_list|,
name|put
operator|.
name|getRow
argument_list|()
argument_list|,
name|transactionState
argument_list|)
block|{
specifier|public
name|Object
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|recordServer
argument_list|()
expr_stmt|;
name|getTransactionServer
argument_list|()
operator|.
name|put
argument_list|(
name|transactionState
operator|.
name|getTransactionId
argument_list|()
argument_list|,
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|put
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
specifier|final
name|TransactionState
name|transactionState
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|ClientScanner
name|scanner
init|=
operator|new
name|TransactionalClientScanner
argument_list|(
name|transactionState
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|initialize
argument_list|()
expr_stmt|;
return|return
name|scanner
return|;
block|}
specifier|protected
class|class
name|TransactionalClientScanner
extends|extends
name|HTable
operator|.
name|ClientScanner
block|{
specifier|private
name|TransactionState
name|transactionState
decl_stmt|;
specifier|protected
name|TransactionalClientScanner
parameter_list|(
specifier|final
name|TransactionState
name|transactionState
parameter_list|,
name|Scan
name|scan
parameter_list|)
block|{
name|super
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|this
operator|.
name|transactionState
operator|=
name|transactionState
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|ScannerCallable
name|getScannerCallable
parameter_list|(
specifier|final
name|byte
index|[]
name|localStartKey
parameter_list|,
name|int
name|caching
parameter_list|)
block|{
name|TransactionScannerCallable
name|t
init|=
operator|new
name|TransactionScannerCallable
argument_list|(
name|transactionState
argument_list|,
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|getScan
argument_list|()
argument_list|)
decl_stmt|;
name|t
operator|.
name|setCaching
argument_list|(
name|caching
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
block|}
block|}
end_class

end_unit

