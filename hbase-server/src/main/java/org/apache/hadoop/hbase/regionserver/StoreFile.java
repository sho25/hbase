begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|OptionalLong
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
name|Cell
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
name|CellComparator
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
name|yetus
operator|.
name|audience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * An interface to describe a store data file.  *<p>  *<strong>NOTICE:</strong>this interface is mainly designed for coprocessor, so it will not expose  * all the internal APIs for a 'store file'. If you are implementing something inside HBase, i.e,  * not a coprocessor hook, usually you should use {@link HStoreFile} directly as it is the only  * implementation of this interface.  */
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
name|StoreFile
block|{
comment|/**    * Get the first key in this store file.    */
name|Optional
argument_list|<
name|Cell
argument_list|>
name|getFirstKey
parameter_list|()
function_decl|;
comment|/**    * Get the last key in this store file.    */
name|Optional
argument_list|<
name|Cell
argument_list|>
name|getLastKey
parameter_list|()
function_decl|;
comment|/**    * Get the comparator for comparing two cells.    */
name|CellComparator
name|getComparator
parameter_list|()
function_decl|;
comment|/**    * Get max of the MemstoreTS in the KV's in this store file.    */
name|long
name|getMaxMemStoreTS
parameter_list|()
function_decl|;
comment|/**    * @return Path or null if this StoreFile was made with a Stream.    */
name|Path
name|getPath
parameter_list|()
function_decl|;
comment|/**    * @return Returns the qualified path of this StoreFile    */
name|Path
name|getQualifiedPath
parameter_list|()
function_decl|;
comment|/**    * @return True if this is a StoreFile Reference.    */
name|boolean
name|isReference
parameter_list|()
function_decl|;
comment|/**    * @return True if this is HFile.    */
name|boolean
name|isHFile
parameter_list|()
function_decl|;
comment|/**    * @return True if this file was made by a major compaction.    */
name|boolean
name|isMajorCompactionResult
parameter_list|()
function_decl|;
comment|/**    * @return True if this file should not be part of a minor compaction.    */
name|boolean
name|excludeFromMinorCompaction
parameter_list|()
function_decl|;
comment|/**    * @return This files maximum edit sequence id.    */
name|long
name|getMaxSequenceId
parameter_list|()
function_decl|;
comment|/**    * Get the modification time of this store file. Usually will access the file system so throws    * IOException.    */
name|long
name|getModificationTimestamp
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Check if this storefile was created by bulk load. When a hfile is bulk loaded into HBase, we    * append {@code '_SeqId_<id-when-loaded>'} to the hfile name, unless    * "hbase.mapreduce.bulkload.assign.sequenceNumbers" is explicitly turned off. If    * "hbase.mapreduce.bulkload.assign.sequenceNumbers" is turned off, fall back to    * BULKLOAD_TIME_KEY.    * @return true if this storefile was created by bulk load.    */
name|boolean
name|isBulkLoadResult
parameter_list|()
function_decl|;
comment|/**    * Return the timestamp at which this bulk load file was generated.    */
name|OptionalLong
name|getBulkLoadTimestamp
parameter_list|()
function_decl|;
comment|/**    * @return a length description of this StoreFile, suitable for debug output    */
name|String
name|toStringDetailed
parameter_list|()
function_decl|;
comment|/**    * Get the min timestamp of all the cells in the store file.    */
name|OptionalLong
name|getMinimumTimestamp
parameter_list|()
function_decl|;
comment|/**    * Get the max timestamp of all the cells in the store file.    */
name|OptionalLong
name|getMaximumTimestamp
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

