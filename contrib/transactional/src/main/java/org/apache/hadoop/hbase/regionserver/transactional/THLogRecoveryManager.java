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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|conf
operator|.
name|Configuration
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
name|WALEdit
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
name|transactional
operator|.
name|HBaseBackedTransactionLogger
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
name|transactional
operator|.
name|TransactionLogger
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
name|WALEdit
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
name|util
operator|.
name|Progressable
import|;
end_import

begin_comment
comment|/**  * Responsible recovering transactional information from the HLog.  */
end_comment

begin_class
class|class
name|THLogRecoveryManager
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
name|THLogRecoveryManager
operator|.
name|class
argument_list|)
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
name|Configuration
name|conf
decl_stmt|;
comment|/**    * @param region    */
specifier|public
name|THLogRecoveryManager
parameter_list|(
specifier|final
name|TransactionalRegion
name|region
parameter_list|)
block|{
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
name|THLogRecoveryManager
parameter_list|(
specifier|final
name|FileSystem
name|fileSystem
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
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
comment|/**    * Go through the WAL, and look for transactions that were started, but never    * completed. If the transaction was committed, then those edits will need to    * be applied.    *     * @param reconstructionLog    * @param maxSeqID    * @param reporter    * @return map of batch updates    * @throws UnsupportedEncodingException    * @throws IOException    */
specifier|public
name|Map
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|WALEdit
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
name|WALEdit
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
name|WALEdit
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Long
argument_list|>
name|commitedTransactions
init|=
operator|new
name|HashSet
argument_list|<
name|Long
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
name|HLog
operator|.
name|Reader
name|reader
init|=
name|HLog
operator|.
name|getReader
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
name|HLog
operator|.
name|Entry
name|entry
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|THLogKey
name|key
init|=
operator|(
name|THLogKey
operator|)
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|WALEdit
name|val
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
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
block|}
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
if|if
condition|(
name|key
operator|.
name|getTrxOp
argument_list|()
operator|==
literal|null
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
name|long
name|transactionId
init|=
name|key
operator|.
name|getTransactionId
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|WALEdit
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
name|key
operator|.
name|getTrxOp
argument_list|()
condition|)
block|{
case|case
name|OP
case|:
if|if
condition|(
name|updates
operator|==
literal|null
condition|)
block|{
name|updates
operator|=
operator|new
name|ArrayList
argument_list|<
name|WALEdit
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
block|}
name|updates
operator|.
name|add
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|val
operator|=
operator|new
name|WALEdit
argument_list|()
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
name|commitedTransactions
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
name|commitedTransactions
operator|.
name|add
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
name|commitCount
operator|++
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unexpected log entry type"
argument_list|)
throw|;
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
name|reader
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
return|return
name|resolvePendingTransaction
argument_list|(
name|pendingTransactionsById
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|WALEdit
argument_list|>
argument_list|>
name|resolvePendingTransaction
parameter_list|(
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|WALEdit
argument_list|>
argument_list|>
name|pendingTransactionsById
parameter_list|)
block|{
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|WALEdit
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
name|WALEdit
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
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
for|for
control|(
name|Entry
argument_list|<
name|Long
argument_list|,
name|List
argument_list|<
name|WALEdit
argument_list|>
argument_list|>
name|entry
range|:
name|pendingTransactionsById
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping resolving trx ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] has no writes."
argument_list|)
expr_stmt|;
block|}
name|TransactionLogger
operator|.
name|TransactionStatus
name|transactionStatus
init|=
name|getGlobalTransactionLog
argument_list|()
operator|.
name|getStatusForTransaction
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|transactionStatus
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot resolve tranasction ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] from global tx log."
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|transactionStatus
condition|)
block|{
case|case
name|ABORTED
case|:
break|break;
case|case
name|COMMITTED
case|:
name|commitedTransactionsById
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|PENDING
case|:
name|LOG
operator|.
name|warn
argument_list|(
literal|"Transaction ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] is still pending. Asumming it will not commit."
operator|+
literal|" If it eventually does commit, then we loose transactional semantics."
argument_list|)
expr_stmt|;
comment|// TODO this could possibly be handled by waiting and seeing what happens.
break|break;
block|}
block|}
return|return
name|commitedTransactionsById
return|;
block|}
specifier|private
name|TransactionLogger
name|globalTransactionLog
init|=
literal|null
decl_stmt|;
specifier|private
specifier|synchronized
name|TransactionLogger
name|getGlobalTransactionLog
parameter_list|()
block|{
if|if
condition|(
name|globalTransactionLog
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|globalTransactionLog
operator|=
operator|new
name|HBaseBackedTransactionLogger
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|globalTransactionLog
return|;
block|}
block|}
end_class

end_unit

