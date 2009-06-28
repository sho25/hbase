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

begin_comment
comment|/**  * Simple interface used to provide a log about transaction status. Written to  * by the client, and read by regionservers in case of failure.   *   */
end_comment

begin_interface
specifier|public
interface|interface
name|TransactionLogger
block|{
comment|/** Transaction status values */
enum|enum
name|TransactionStatus
block|{
comment|/** Transaction is pending */
name|PENDING
block|,
comment|/** Transaction was committed */
name|COMMITTED
block|,
comment|/** Transaction was aborted */
name|ABORTED
block|}
comment|/**    * Create a new transaction log. Return the transaction's globally unique id.    * Log's initial value should be PENDING    *     * @return transaction id    */
name|long
name|createNewTransactionLog
parameter_list|()
function_decl|;
comment|/**    * @param transactionId    * @return transaction status    */
name|TransactionStatus
name|getStatusForTransaction
parameter_list|(
name|long
name|transactionId
parameter_list|)
function_decl|;
comment|/**    * @param transactionId    * @param status    */
name|void
name|setStatusForTransaction
parameter_list|(
name|long
name|transactionId
parameter_list|,
name|TransactionStatus
name|status
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

