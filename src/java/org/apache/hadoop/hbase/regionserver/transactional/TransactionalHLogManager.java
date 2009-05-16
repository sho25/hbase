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
comment|/**  * Responsible for writing and reading (recovering) transactional information  * to/from the HLog.  */
end_comment

begin_class
class|class
name|TransactionalHLogManager
block|{
comment|/** If transactional log entry, these are the op codes */
comment|// TODO: Make these into types on the KeyValue!!! -- St.Ack
specifier|public
enum|enum
name|TransactionalOperation
block|{
comment|/** start transaction */
name|START
block|,
comment|/** Equivalent to append in non-transactional environment */
name|WRITE
block|,
comment|/** Transaction commit entry */
name|COMMIT
block|,
comment|/** Abort transaction entry */
name|ABORT
block|}
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
comment|/*     HLogEdit logEdit;     logEdit = new HLogEdit(transactionId, TransactionalOperation.START); */
name|hlog
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
literal|null
comment|/*logEdit*/
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
comment|// COMMENTED OUT  HLogEdit logEdit = new HLogEdit(transactionId, update.getRow(), op, commitTime);
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
literal|null
comment|/*logEdit*/
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
comment|/*HLogEdit logEdit;     logEdit = new HLogEdit(transactionId,         HLogEdit.TransactionalOperation.COMMIT); */
name|hlog
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
literal|null
comment|/*logEdit*/
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
comment|/*HLogEdit logEdit;     logEdit = new HLogEdit(transactionId, HLogEdit.TransactionalOperation.ABORT); */
name|hlog
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
literal|null
comment|/*logEdit*/
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
comment|/*     try {       HLogKey key = new HLogKey();       KeyValue val = new KeyValue();       long skippedEdits = 0;       long totalEdits = 0;       long startCount = 0;       long writeCount = 0;       long abortCount = 0;       long commitCount = 0;       // How many edits to apply before we send a progress report.       int reportInterval = conf.getInt("hbase.hstore.report.interval.edits",           2000);        while (logReader.next(key, val)) {         LOG.debug("Processing edit: key: " + key.toString() + " val: "             + val.toString());         if (key.getLogSeqNum()< maxSeqID) {           skippedEdits++;           continue;         }         // TODO: Change all below so we are not doing a getRow and getColumn         // against a KeyValue.  Each invocation creates a new instance.  St.Ack.          // Check this edit is for me.          byte[] column = val.getKeyValue().getColumn();         Long transactionId = val.getTransactionId();         if (!val.isTransactionEntry() || HLog.isMetaColumn(column)             || !Bytes.equals(key.getRegionName(), regionInfo.getRegionName())) {           continue;         }          List<BatchUpdate> updates = pendingTransactionsById.get(transactionId);         switch (val.getOperation()) {          case START:           if (updates != null || abortedTransactions.contains(transactionId)               || commitedTransactionsById.containsKey(transactionId)) {             LOG.error("Processing start for transaction: " + transactionId                 + ", but have already seen start message");             throw new IOException("Corrupted transaction log");           }           updates = new LinkedList<BatchUpdate>();           pendingTransactionsById.put(transactionId, updates);           startCount++;           break;          case WRITE:           if (updates == null) {             LOG.error("Processing edit for transaction: " + transactionId                 + ", but have not seen start message");             throw new IOException("Corrupted transaction log");           }            BatchUpdate tranUpdate = new BatchUpdate(val.getKeyValue().getRow());           if (val.getKeyValue().getValue() != null) {             tranUpdate.put(val.getKeyValue().getColumn(),               val.getKeyValue().getValue());           } else {             tranUpdate.delete(val.getKeyValue().getColumn());           }           updates.add(tranUpdate);           writeCount++;           break;          case ABORT:           if (updates == null) {             LOG.error("Processing abort for transaction: " + transactionId                 + ", but have not seen start message");             throw new IOException("Corrupted transaction log");           }           abortedTransactions.add(transactionId);           pendingTransactionsById.remove(transactionId);           abortCount++;           break;          case COMMIT:           if (updates == null) {             LOG.error("Processing commit for transaction: " + transactionId                 + ", but have not seen start message");             throw new IOException("Corrupted transaction log");           }           if (abortedTransactions.contains(transactionId)) {             LOG.error("Processing commit for transaction: " + transactionId                 + ", but also have abort message");             throw new IOException("Corrupted transaction log");           }           if (updates.size() == 0) {             LOG                 .warn("Transaciton " + transactionId                     + " has no writes in log. ");           }           if (commitedTransactionsById.containsKey(transactionId)) {             LOG.error("Processing commit for transaction: " + transactionId                 + ", but have already commited transaction with that id");             throw new IOException("Corrupted transaction log");           }           pendingTransactionsById.remove(transactionId);           commitedTransactionsById.put(transactionId, updates);           commitCount++;          }         totalEdits++;          if (reporter != null&& (totalEdits % reportInterval) == 0) {           reporter.progress();         }       }       if (LOG.isDebugEnabled()) {         LOG.debug("Read " + totalEdits + " tranasctional operations (skipped "             + skippedEdits + " because sequence id<= " + maxSeqID + "): "             + startCount + " starts, " + writeCount + " writes, " + abortCount             + " aborts, and " + commitCount + " commits.");       }     } finally {       logReader.close();     }      if (pendingTransactionsById.size()> 0) {       LOG           .info("Region log has "               + pendingTransactionsById.size()               + " unfinished transactions. Going to the transaction log to resolve");       throw new RuntimeException("Transaction log not yet implemented");     }               */
return|return
name|commitedTransactionsById
return|;
block|}
block|}
end_class

end_unit

