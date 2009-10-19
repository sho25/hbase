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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|HRegionInfo
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
name|KeyValue
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|LogRollListener
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
name|io
operator|.
name|SequenceFile
import|;
end_import

begin_comment
comment|/**  * Add support for transactional operations to the regionserver's  * write-ahead-log.  *   */
end_comment

begin_class
class|class
name|THLog
extends|extends
name|HLog
block|{
specifier|public
name|THLog
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|,
name|HBaseConfiguration
name|conf
parameter_list|,
name|LogRollListener
name|listener
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|conf
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|SequenceFile
operator|.
name|Writer
name|createWriter
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|createWriter
argument_list|(
name|path
argument_list|,
name|THLogKey
operator|.
name|class
argument_list|,
name|KeyValue
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HLogKey
name|makeKey
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|long
name|seqNum
parameter_list|,
name|long
name|now
parameter_list|)
block|{
return|return
operator|new
name|THLogKey
argument_list|(
name|regionName
argument_list|,
name|tableName
argument_list|,
name|seqNum
argument_list|,
name|now
argument_list|)
return|;
block|}
specifier|public
name|void
name|writeUpdateToLog
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|long
name|transactionId
parameter_list|,
specifier|final
name|Put
name|update
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|update
argument_list|,
name|transactionId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeDeleteToLog
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|long
name|transactionId
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|delete
argument_list|,
name|transactionId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeCommitToLog
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|THLogKey
operator|.
name|TrxOp
operator|.
name|COMMIT
argument_list|,
name|transactionId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeAbortToLog
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|THLogKey
operator|.
name|TrxOp
operator|.
name|ABORT
argument_list|,
name|transactionId
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write a general transaction op to the log. This covers: start, commit, and    * abort.    *     * @param regionInfo    * @param now    * @param txOp    * @param transactionId    * @throws IOException    */
specifier|public
name|void
name|append
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|long
name|now
parameter_list|,
name|THLogKey
operator|.
name|TrxOp
name|txOp
parameter_list|,
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|THLogKey
name|key
init|=
operator|new
name|THLogKey
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
name|now
argument_list|,
name|txOp
argument_list|,
name|transactionId
argument_list|)
decl_stmt|;
name|super
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|key
argument_list|,
operator|new
name|KeyValue
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Empty KeyValue
block|}
comment|/**    * Write a transactional update to the log.    *     * @param regionInfo    * @param now    * @param update    * @param transactionId    * @throws IOException    */
specifier|public
name|void
name|append
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|Put
name|update
parameter_list|,
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|commitTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|THLogKey
name|key
init|=
operator|new
name|THLogKey
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
name|commitTime
argument_list|,
name|THLogKey
operator|.
name|TrxOp
operator|.
name|OP
argument_list|,
name|transactionId
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|value
range|:
name|convertToKeyValues
argument_list|(
name|update
argument_list|)
control|)
block|{
name|super
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Write a transactional delete to the log.    *     * @param regionInfo    * @param now    * @param update    * @param transactionId    * @throws IOException    */
specifier|public
name|void
name|append
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|Delete
name|delete
parameter_list|,
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|commitTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|THLogKey
name|key
init|=
operator|new
name|THLogKey
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
name|commitTime
argument_list|,
name|THLogKey
operator|.
name|TrxOp
operator|.
name|OP
argument_list|,
name|transactionId
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|value
range|:
name|convertToKeyValues
argument_list|(
name|delete
argument_list|)
control|)
block|{
name|super
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|KeyValue
argument_list|>
name|convertToKeyValues
parameter_list|(
name|Put
name|update
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|edits
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
range|:
name|update
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|edits
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|edits
return|;
block|}
specifier|private
name|List
argument_list|<
name|KeyValue
argument_list|>
name|convertToKeyValues
parameter_list|(
name|Delete
name|delete
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|edits
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
range|:
name|delete
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|edits
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|edits
return|;
block|}
block|}
end_class

end_unit

