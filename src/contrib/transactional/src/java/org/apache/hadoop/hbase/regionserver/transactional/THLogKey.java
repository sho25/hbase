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
name|regionserver
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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|regionserver
operator|.
name|wal
operator|.
name|HLogKey
import|;
end_import

begin_class
specifier|public
class|class
name|THLogKey
extends|extends
name|HLogKey
block|{
comment|/** Type of Transactional op going into the HLot    *      */
specifier|public
enum|enum
name|TrxOp
block|{
comment|/** A standard operation that is transactional. KV holds the op. */
name|OP
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|,
comment|/** A transaction was committed. */
name|COMMIT
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|)
block|,
comment|/** A transaction was aborted. */
name|ABORT
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|)
block|;
specifier|private
specifier|final
name|byte
name|opCode
decl_stmt|;
specifier|private
name|TrxOp
parameter_list|(
name|byte
name|opCode
parameter_list|)
block|{
name|this
operator|.
name|opCode
operator|=
name|opCode
expr_stmt|;
block|}
specifier|public
specifier|static
name|TrxOp
name|fromByte
parameter_list|(
name|byte
name|opCode
parameter_list|)
block|{
for|for
control|(
name|TrxOp
name|op
range|:
name|TrxOp
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|op
operator|.
name|opCode
operator|==
name|opCode
condition|)
block|{
return|return
name|op
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|byte
name|transactionOp
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|transactionId
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|THLogKey
parameter_list|()
block|{
comment|// For Writable
block|}
specifier|public
name|THLogKey
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|tablename
parameter_list|,
name|long
name|logSeqNum
parameter_list|,
name|long
name|now
parameter_list|)
block|{
name|super
argument_list|(
name|regionName
argument_list|,
name|tablename
argument_list|,
name|logSeqNum
argument_list|,
name|now
argument_list|)
expr_stmt|;
block|}
specifier|public
name|THLogKey
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|tablename
parameter_list|,
name|long
name|logSeqNum
parameter_list|,
name|long
name|now
parameter_list|,
name|TrxOp
name|op
parameter_list|,
name|long
name|transactionId
parameter_list|)
block|{
name|super
argument_list|(
name|regionName
argument_list|,
name|tablename
argument_list|,
name|logSeqNum
argument_list|,
name|now
argument_list|)
expr_stmt|;
name|this
operator|.
name|transactionOp
operator|=
name|op
operator|.
name|opCode
expr_stmt|;
name|this
operator|.
name|transactionId
operator|=
name|transactionId
expr_stmt|;
block|}
specifier|public
name|TrxOp
name|getTrxOp
parameter_list|()
block|{
return|return
name|TrxOp
operator|.
name|fromByte
argument_list|(
name|this
operator|.
name|transactionOp
argument_list|)
return|;
block|}
specifier|public
name|long
name|getTransactionId
parameter_list|()
block|{
return|return
name|this
operator|.
name|transactionId
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|transactionOp
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|transactionOp
operator|=
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|this
operator|.
name|transactionId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

