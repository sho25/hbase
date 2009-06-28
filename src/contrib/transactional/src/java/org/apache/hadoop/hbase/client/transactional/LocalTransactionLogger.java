begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_comment
comment|/**  * A local, in-memory implementation of the transaction logger. Does not provide a global view, so   * it can't be relighed on by   *   */
end_comment

begin_class
specifier|public
class|class
name|LocalTransactionLogger
implements|implements
name|TransactionLogger
block|{
specifier|private
specifier|static
name|LocalTransactionLogger
name|instance
decl_stmt|;
comment|/**    * Creates singleton if it does not exist    *     * @return reference to singleton    */
specifier|public
specifier|synchronized
specifier|static
name|LocalTransactionLogger
name|getInstance
parameter_list|()
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
operator|new
name|LocalTransactionLogger
argument_list|()
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Long
argument_list|,
name|TransactionStatus
argument_list|>
name|transactionIdToStatusMap
init|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|TransactionStatus
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|LocalTransactionLogger
parameter_list|()
block|{
comment|// Enforce singlton
block|}
comment|/** @return random longs to minimize possibility of collision */
specifier|public
name|long
name|createNewTransactionLog
parameter_list|()
block|{
name|long
name|id
decl_stmt|;
do|do
block|{
name|id
operator|=
name|random
operator|.
name|nextLong
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|transactionIdToStatusMap
operator|.
name|containsKey
argument_list|(
name|id
argument_list|)
condition|)
do|;
name|transactionIdToStatusMap
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|TransactionStatus
operator|.
name|PENDING
argument_list|)
expr_stmt|;
return|return
name|id
return|;
block|}
specifier|public
name|TransactionStatus
name|getStatusForTransaction
parameter_list|(
specifier|final
name|long
name|transactionId
parameter_list|)
block|{
return|return
name|transactionIdToStatusMap
operator|.
name|get
argument_list|(
name|transactionId
argument_list|)
return|;
block|}
specifier|public
name|void
name|setStatusForTransaction
parameter_list|(
specifier|final
name|long
name|transactionId
parameter_list|,
specifier|final
name|TransactionStatus
name|status
parameter_list|)
block|{
name|transactionIdToStatusMap
operator|.
name|put
argument_list|(
name|transactionId
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|forgetTransaction
parameter_list|(
name|long
name|transactionId
parameter_list|)
block|{
name|transactionIdToStatusMap
operator|.
name|remove
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

