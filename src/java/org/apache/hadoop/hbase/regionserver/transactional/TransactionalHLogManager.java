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
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|FileStatus
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
name|HConstants
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
name|io
operator|.
name|BatchOperation
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
name|io
operator|.
name|BatchUpdate
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
name|HLogEdit
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
name|util
operator|.
name|Bytes
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Progressable
import|;
end_import

begin_comment
comment|/**  * Responsible for writing and reading (recovering) transactional information  * to/from the HLog.  *   *   */
end_comment

begin_class
class|class
name|TransactionalHLogManager
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TransactionalHLogManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HLog
name|hlog
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fileSystem
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
specifier|final
name|HBaseConfiguration
name|conf
decl_stmt|;
comment|/**    * @param region    */
specifier|public
name|TransactionalHLogManager
parameter_list|(
specifier|final
name|TransactionalRegion
name|region
parameter_list|)
block|{
name|this
operator|.
name|hlog
operator|=
name|region
operator|.
name|getLog
argument_list|()
expr_stmt|;
name|this
operator|.
name|fileSystem
operator|=
name|region
operator|.
name|getFilesystem
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|region
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|region
operator|.
name|getConf
argument_list|()
expr_stmt|;
block|}
comment|// For Testing
name|TransactionalHLogManager
parameter_list|(
specifier|final
name|HLog
name|hlog
parameter_list|,
specifier|final
name|FileSystem
name|fileSystem
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|hlog
operator|=
name|hlog
expr_stmt|;
name|this
operator|.
name|fileSystem
operator|=
name|fileSystem
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * @param transactionId    * @throws IOException    */
specifier|public
name|void
name|writeStartToLog
parameter_list|(
specifier|final
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|HLogEdit
name|logEdit
decl_stmt|;
name|logEdit
operator|=
operator|new
name|HLogEdit
argument_list|(
name|transactionId
argument_list|,
name|HLogEdit
operator|.
name|TransactionalOperation
operator|.
name|START
argument_list|)
expr_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param transactionId    * @param update    * @throws IOException    */
specifier|public
name|void
name|writeUpdateToLog
parameter_list|(
specifier|final
name|long
name|transactionId
parameter_list|,
specifier|final
name|BatchUpdate
name|update
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|commitTime
init|=
name|update
operator|.
name|getTimestamp
argument_list|()
operator|==
name|HConstants
operator|.
name|LATEST_TIMESTAMP
condition|?
name|System
operator|.
name|currentTimeMillis
argument_list|()
else|:
name|update
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
for|for
control|(
name|BatchOperation
name|op
range|:
name|update
control|)
block|{
name|HLogEdit
name|logEdit
init|=
operator|new
name|HLogEdit
argument_list|(
name|transactionId
argument_list|,
name|op
argument_list|,
name|commitTime
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|update
operator|.
name|getRow
argument_list|()
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param transactionId    * @throws IOException    */
specifier|public
name|void
name|writeCommitToLog
parameter_list|(
specifier|final
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|HLogEdit
name|logEdit
decl_stmt|;
name|logEdit
operator|=
operator|new
name|HLogEdit
argument_list|(
name|transactionId
argument_list|,
name|HLogEdit
operator|.
name|TransactionalOperation
operator|.
name|COMMIT
argument_list|)
expr_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param transactionId    * @throws IOException    */
specifier|public
name|void
name|writeAbortToLog
parameter_list|(
specifier|final
name|long
name|transactionId
parameter_list|)
throws|throws
name|IOException
block|{
name|HLogEdit
name|logEdit
decl_stmt|;
name|logEdit
operator|=
operator|new
name|HLogEdit
argument_list|(
name|transactionId
argument_list|,
name|HLogEdit
operator|.
name|TransactionalOperation
operator|.
name|ABORT
argument_list|)
expr_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param reconstructionLog    * @param maxSeqID    * @param reporter    * @return map of batch updates    * @throws UnsupportedEncodingException    * @throws IOException    */
specifier|public
name|Map
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|BatchUpdate
argument_list|>
argument_list|>
name|getCommitsFromLog
parameter_list|(
specifier|final
name|Path
name|reconstructionLog
parameter_list|,
specifier|final
name|long
name|maxSeqID
parameter_list|,
specifier|final
name|Progressable
name|reporter
parameter_list|)
throws|throws
name|UnsupportedEncodingException
throws|,
name|IOException
block|{
if|if
condition|(
name|reconstructionLog
operator|==
literal|null
operator|||
operator|!
name|fileSystem
operator|.
name|exists
argument_list|(
name|reconstructionLog
argument_list|)
condition|)
block|{
comment|// Nothing to do.
return|return
literal|null
return|;
block|}
comment|// Check its not empty.
name|FileStatus
index|[]
name|stats
init|=
name|fileSystem
operator|.
name|listStatus
argument_list|(
name|reconstructionLog
argument_list|)
decl_stmt|;
if|if
condition|(
name|stats
operator|==
literal|null
operator|||
name|stats
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Passed reconstruction log "
operator|+
name|reconstructionLog
operator|+
literal|" is zero-length"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|BatchUpdate
argument_list|>
argument_list|>
name|pendingTransactionsById
init|=
operator|new
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|BatchUpdate
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|BatchUpdate
argument_list|>
argument_list|>
name|commitedTransactionsById
init|=
operator|new
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|BatchUpdate
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Long
argument_list|>
name|abortedTransactions
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|SequenceFile
operator|.
name|Reader
name|logReader
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fileSystem
argument_list|,
name|reconstructionLog
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|()
decl_stmt|;
name|HLogEdit
name|val
init|=
operator|new
name|HLogEdit
argument_list|()
decl_stmt|;
name|long
name|skippedEdits
init|=
literal|0
decl_stmt|;
name|long
name|totalEdits
init|=
literal|0
decl_stmt|;
name|long
name|startCount
init|=
literal|0
decl_stmt|;
name|long
name|writeCount
init|=
literal|0
decl_stmt|;
name|long
name|abortCount
init|=
literal|0
decl_stmt|;
name|long
name|commitCount
init|=
literal|0
decl_stmt|;
comment|// How many edits to apply before we send a progress report.
name|int
name|reportInterval
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hstore.report.interval.edits"
argument_list|,
literal|2000
argument_list|)
decl_stmt|;
while|while
condition|(
name|logReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing edit: key: "
operator|+
name|key
operator|.
name|toString
argument_list|()
operator|+
literal|" val: "
operator|+
name|val
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|key
operator|.
name|getLogSeqNum
argument_list|()
operator|<
name|maxSeqID
condition|)
block|{
name|skippedEdits
operator|++
expr_stmt|;
continue|continue;
block|}
comment|// Check this edit is for me.
name|byte
index|[]
name|column
init|=
name|val
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|Long
name|transactionId
init|=
name|val
operator|.
name|getTransactionId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|val
operator|.
name|isTransactionEntry
argument_list|()
operator|||
name|HLog
operator|.
name|isMetaColumn
argument_list|(
name|column
argument_list|)
operator|||
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|key
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|List
argument_list|<
name|BatchUpdate
argument_list|>
name|updates
init|=
name|pendingTransactionsById
operator|.
name|get
argument_list|(
name|transactionId
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|val
operator|.
name|getOperation
argument_list|()
condition|)
block|{
case|case
name|START
case|:
if|if
condition|(
name|updates
operator|!=
literal|null
operator|||
name|abortedTransactions
operator|.
name|contains
argument_list|(
name|transactionId
argument_list|)
operator|||
name|commitedTransactionsById
operator|.
name|containsKey
argument_list|(
name|transactionId
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Processing start for transaction: "
operator|+
name|transactionId
operator|+
literal|", but have already seen start message"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Corrupted transaction log"
argument_list|)
throw|;
block|}
name|updates
operator|=
operator|new
name|LinkedList
argument_list|<
name|BatchUpdate
argument_list|>
argument_list|()
expr_stmt|;
name|pendingTransactionsById
operator|.
name|put
argument_list|(
name|transactionId
argument_list|,
name|updates
argument_list|)
expr_stmt|;
name|startCount
operator|++
expr_stmt|;
break|break;
case|case
name|WRITE
case|:
if|if
condition|(
name|updates
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Processing edit for transaction: "
operator|+
name|transactionId
operator|+
literal|", but have not seen start message"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Corrupted transaction log"
argument_list|)
throw|;
block|}
name|BatchUpdate
name|tranUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|.
name|getVal
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tranUpdate
operator|.
name|put
argument_list|(
name|val
operator|.
name|getColumn
argument_list|()
argument_list|,
name|val
operator|.
name|getVal
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tranUpdate
operator|.
name|delete
argument_list|(
name|val
operator|.
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|updates
operator|.
name|add
argument_list|(
name|tranUpdate
argument_list|)
expr_stmt|;
name|writeCount
operator|++
expr_stmt|;
break|break;
case|case
name|ABORT
case|:
if|if
condition|(
name|updates
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Processing abort for transaction: "
operator|+
name|transactionId
operator|+
literal|", but have not seen start message"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Corrupted transaction log"
argument_list|)
throw|;
block|}
name|abortedTransactions
operator|.
name|add
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
name|pendingTransactionsById
operator|.
name|remove
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
name|abortCount
operator|++
expr_stmt|;
break|break;
case|case
name|COMMIT
case|:
if|if
condition|(
name|updates
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Processing commit for transaction: "
operator|+
name|transactionId
operator|+
literal|", but have not seen start message"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Corrupted transaction log"
argument_list|)
throw|;
block|}
if|if
condition|(
name|abortedTransactions
operator|.
name|contains
argument_list|(
name|transactionId
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Processing commit for transaction: "
operator|+
name|transactionId
operator|+
literal|", but also have abort message"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Corrupted transaction log"
argument_list|)
throw|;
block|}
if|if
condition|(
name|updates
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Transaciton "
operator|+
name|transactionId
operator|+
literal|" has no writes in log. "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|commitedTransactionsById
operator|.
name|containsKey
argument_list|(
name|transactionId
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Processing commit for transaction: "
operator|+
name|transactionId
operator|+
literal|", but have already commited transaction with that id"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Corrupted transaction log"
argument_list|)
throw|;
block|}
name|pendingTransactionsById
operator|.
name|remove
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
name|commitedTransactionsById
operator|.
name|put
argument_list|(
name|transactionId
argument_list|,
name|updates
argument_list|)
expr_stmt|;
name|commitCount
operator|++
expr_stmt|;
block|}
name|totalEdits
operator|++
expr_stmt|;
if|if
condition|(
name|reporter
operator|!=
literal|null
operator|&&
operator|(
name|totalEdits
operator|%
name|reportInterval
operator|)
operator|==
literal|0
condition|)
block|{
name|reporter
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Read "
operator|+
name|totalEdits
operator|+
literal|" tranasctional operations (skipped "
operator|+
name|skippedEdits
operator|+
literal|" because sequence id<= "
operator|+
name|maxSeqID
operator|+
literal|"): "
operator|+
name|startCount
operator|+
literal|" starts, "
operator|+
name|writeCount
operator|+
literal|" writes, "
operator|+
name|abortCount
operator|+
literal|" aborts, and "
operator|+
name|commitCount
operator|+
literal|" commits."
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|logReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|pendingTransactionsById
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region log has "
operator|+
name|pendingTransactionsById
operator|.
name|size
argument_list|()
operator|+
literal|" unfinished transactions. Going to the transaction log to resolve"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Transaction log not yet implemented"
argument_list|)
throw|;
block|}
return|return
name|commitedTransactionsById
return|;
block|}
block|}
end_class

end_unit

