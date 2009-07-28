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
name|ipc
operator|.
name|TransactionalRegionInterface
import|;
end_import

begin_class
class|class
name|TransactionScannerCallable
extends|extends
name|ScannerCallable
block|{
specifier|private
name|TransactionState
name|transactionState
decl_stmt|;
name|TransactionScannerCallable
parameter_list|(
specifier|final
name|TransactionState
name|transactionState
parameter_list|,
specifier|final
name|HConnection
name|connection
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|Scan
name|scan
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
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
name|long
name|openScanner
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
operator|(
operator|(
name|TransactionalRegionInterface
operator|)
name|server
operator|)
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
return|return
operator|(
operator|(
name|TransactionalRegionInterface
operator|)
name|server
operator|)
operator|.
name|openScanner
argument_list|(
name|transactionState
operator|.
name|getTransactionId
argument_list|()
argument_list|,
name|this
operator|.
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|getScan
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

