begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|SplitTransaction
operator|.
name|TransactionListener
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
name|HBaseInterfaceAudience
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
name|Server
import|;
end_import

begin_comment
comment|/**  * Executes region merge as a "transaction". It is similar with  * SplitTransaction. Call {@link #prepare(RegionServerServices)} to setup the  * transaction, {@link #execute(Server, RegionServerServices)} to run the  * transaction and {@link #rollback(Server, RegionServerServices)} to cleanup if  * execute fails.  *   *<p>Here is an example of how you would use this interface:  *<pre>  *  RegionMergeTransactionFactory factory = new RegionMergeTransactionFactory(conf);  *  RegionMergeTransaction mt = factory.create(parent, midKey)  *    .registerTransactionListener(new TransactionListener() {  *       public void transition(RegionMergeTransaction transaction,  *         RegionMergeTransactionPhase from, RegionMergeTransactionPhase to) throws IOException {  *         // ...  *       }  *       public void rollback(RegionMergeTransaction transaction,  *         RegionMergeTransactionPhase from, RegionMergeTransactionPhase to) {  *         // ...  *       }  *    });  *  if (!mt.prepare()) return;  *  try {  *    mt.execute(server, services);  *  } catch (IOException ioe) {  *    try {  *      mt.rollback(server, services);  *      return;  *    } catch (RuntimeException e) {  *      // abort the server  *    }  *  }  *</Pre>  *<p>A merge transaction is not thread safe.  Callers must ensure a split is run by  * one thread only.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|RegionMergeTransaction
block|{
comment|/**    * Each enum is a step in the merge transaction.    */
enum|enum
name|RegionMergeTransactionPhase
block|{
name|STARTED
block|,
comment|/**      * Prepared      */
name|PREPARED
block|,
comment|/**      * Set region as in transition, set it into MERGING state.      */
name|SET_MERGING
block|,
comment|/**      * We created the temporary merge data directory.      */
name|CREATED_MERGE_DIR
block|,
comment|/**      * Closed the merging region A.      */
name|CLOSED_REGION_A
block|,
comment|/**      * The merging region A has been taken out of the server's online regions list.      */
name|OFFLINED_REGION_A
block|,
comment|/**      * Closed the merging region B.      */
name|CLOSED_REGION_B
block|,
comment|/**      * The merging region B has been taken out of the server's online regions list.      */
name|OFFLINED_REGION_B
block|,
comment|/**      * Started in on creation of the merged region.      */
name|STARTED_MERGED_REGION_CREATION
block|,
comment|/**      * Point of no return. If we got here, then transaction is not recoverable      * other than by crashing out the regionserver.      */
name|PONR
block|,
comment|/**      * Completed      */
name|COMPLETED
block|}
comment|/**    * Split transaction journal entry    */
specifier|public
interface|interface
name|JournalEntry
block|{
comment|/** @return the completed phase marked by this journal entry */
name|RegionMergeTransactionPhase
name|getPhase
parameter_list|()
function_decl|;
comment|/** @return the time of phase completion */
name|long
name|getTimeStamp
parameter_list|()
function_decl|;
block|}
comment|/**    * Split transaction listener    */
specifier|public
interface|interface
name|TransactionListener
block|{
comment|/**      * Invoked when transitioning forward from one transaction phase to another      * @param transaction the transaction      * @param from the current phase      * @param to the next phase      * @throws IOException listener can throw this to abort      */
name|void
name|transition
parameter_list|(
name|RegionMergeTransaction
name|transaction
parameter_list|,
name|RegionMergeTransactionPhase
name|from
parameter_list|,
name|RegionMergeTransactionPhase
name|to
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Invoked when rolling back a transaction from one transaction phase to the      * previous      * @param transaction the transaction      * @param from the current phase      * @param to the previous phase      */
name|void
name|rollback
parameter_list|(
name|RegionMergeTransaction
name|transaction
parameter_list|,
name|RegionMergeTransactionPhase
name|from
parameter_list|,
name|RegionMergeTransactionPhase
name|to
parameter_list|)
function_decl|;
block|}
comment|/**    * Check merge inputs and prepare the transaction.    * @param services    * @return<code>true</code> if the regions are mergeable else    *<code>false</code> if they are not (e.g. its already closed, etc.).    * @throws IOException     */
name|boolean
name|prepare
parameter_list|(
name|RegionServerServices
name|services
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Run the transaction.    * @param server Hosting server instance. Can be null when testing    * @param services Used to online/offline regions.    * @throws IOException If thrown, transaction failed. Call    *           {@link #rollback(Server, RegionServerServices)}    * @return merged region    * @throws IOException    * @see #rollback(Server, RegionServerServices)    */
name|Region
name|execute
parameter_list|(
name|Server
name|server
parameter_list|,
name|RegionServerServices
name|services
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Roll back a failed transaction    * @param server Hosting server instance (May be null when testing).    * @param services Services of regionserver, used to online regions.    * @throws IOException If thrown, rollback failed. Take drastic action.    * @return True if we successfully rolled back, false if we got to the point    *         of no return and so now need to abort the server to minimize    *         damage.    */
name|boolean
name|rollback
parameter_list|(
name|Server
name|server
parameter_list|,
name|RegionServerServices
name|services
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Register a listener for transaction preparation, execution, and possibly    * rollback phases.    *<p>A listener can abort a transaction by throwing an exception.     * @param listener the listener    * @return 'this' for chaining    */
name|RegionMergeTransaction
name|registerTransactionListener
parameter_list|(
name|TransactionListener
name|listener
parameter_list|)
function_decl|;
comment|/** @return merged region info */
name|HRegionInfo
name|getMergedRegionInfo
parameter_list|()
function_decl|;
comment|/**    * Get the journal for the transaction.    *<p>Journal entries are an opaque type represented as JournalEntry. They can    * also provide useful debugging information via their toString method.    * @return the transaction journal    */
name|List
argument_list|<
name|JournalEntry
argument_list|>
name|getJournal
parameter_list|()
function_decl|;
comment|/**    * Get the Server running the transaction or rollback    * @return server instance    */
name|Server
name|getServer
parameter_list|()
function_decl|;
comment|/**    * Get the RegonServerServices of the server running the transaction or rollback    * @return region server services    */
name|RegionServerServices
name|getRegionServerServices
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

