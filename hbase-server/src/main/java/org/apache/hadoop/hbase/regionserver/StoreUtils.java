begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collection
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
name|OptionalInt
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
name|CellUtil
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

begin_comment
comment|/**  * Utility functions for region server storage layer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StoreUtils
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
name|StoreUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Creates a deterministic hash code for store file collection.    */
specifier|public
specifier|static
name|OptionalInt
name|getDeterministicRandomSeed
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|)
block|{
return|return
name|files
operator|.
name|stream
argument_list|()
operator|.
name|mapToInt
argument_list|(
name|f
lambda|->
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|hashCode
argument_list|()
argument_list|)
operator|.
name|findFirst
argument_list|()
return|;
block|}
comment|/**    * Determines whether any files in the collection are references.    * @param files The files.    */
specifier|public
specifier|static
name|boolean
name|hasReferences
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|)
block|{
if|if
condition|(
name|files
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|StoreFile
name|hsf
range|:
name|files
control|)
block|{
if|if
condition|(
name|hsf
operator|.
name|isReference
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Gets lowest timestamp from candidate StoreFiles    */
specifier|public
specifier|static
name|long
name|getLowestTimestamp
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|minTs
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|StoreFile
name|storeFile
range|:
name|candidates
control|)
block|{
name|minTs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minTs
argument_list|,
name|storeFile
operator|.
name|getModificationTimeStamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|minTs
return|;
block|}
comment|/**    * Gets the largest file (with reader) out of the list of files.    * @param candidates The files to choose from.    * @return The largest file; null if no file has a reader.    */
specifier|static
name|Optional
argument_list|<
name|StoreFile
argument_list|>
name|getLargestFile
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|)
block|{
return|return
name|candidates
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|f
lambda|->
name|f
operator|.
name|getReader
argument_list|()
operator|!=
literal|null
argument_list|)
operator|.
name|max
argument_list|(
parameter_list|(
name|f1
parameter_list|,
name|f2
parameter_list|)
lambda|->
name|Long
operator|.
name|compare
argument_list|(
name|f1
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
argument_list|,
name|f2
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Return the largest memstoreTS found across all storefiles in the given list. Store files that    * were created by a mapreduce bulk load are ignored, as they do not correspond to any specific    * put operation, and thus do not have a memstoreTS associated with them.    * @return 0 if no non-bulk-load files are provided or, this is Store that does not yet have any    *         store files.    */
specifier|public
specifier|static
name|long
name|getMaxMemstoreTSInList
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|sfs
parameter_list|)
block|{
name|long
name|max
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|sfs
control|)
block|{
if|if
condition|(
operator|!
name|sf
operator|.
name|isBulkLoadResult
argument_list|()
condition|)
block|{
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|max
argument_list|,
name|sf
operator|.
name|getMaxMemstoreTS
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|max
return|;
block|}
comment|/**    * Return the highest sequence ID found across all storefiles in    * the given list.    * @param sfs    * @return 0 if no non-bulk-load files are provided or, this is Store that    * does not yet have any store files.    */
specifier|public
specifier|static
name|long
name|getMaxSequenceIdInList
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|sfs
parameter_list|)
block|{
name|long
name|max
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|sfs
control|)
block|{
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|max
argument_list|,
name|sf
operator|.
name|getMaxSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|max
return|;
block|}
comment|/**    * Gets the approximate mid-point of the given file that is optimal for use in splitting it.    * @param file the store file    * @param comparator Comparator used to compare KVs.    * @return The split point row, or null if splitting is not possible, or reader is null.    */
specifier|static
name|Optional
argument_list|<
name|byte
index|[]
argument_list|>
name|getFileSplitPoint
parameter_list|(
name|StoreFile
name|file
parameter_list|,
name|CellComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
block|{
name|StoreFileReader
name|reader
init|=
name|file
operator|.
name|getReader
argument_list|()
decl_stmt|;
if|if
condition|(
name|reader
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Storefile "
operator|+
name|file
operator|+
literal|" Reader is null; cannot get split point"
argument_list|)
expr_stmt|;
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
comment|// Get first, last, and mid keys. Midkey is the key that starts block
comment|// in middle of hfile. Has column and timestamp. Need to return just
comment|// the row we want to split on as midkey.
name|Cell
name|midkey
init|=
name|reader
operator|.
name|midkey
argument_list|()
decl_stmt|;
if|if
condition|(
name|midkey
operator|!=
literal|null
condition|)
block|{
name|Cell
name|firstKey
init|=
name|reader
operator|.
name|getFirstKey
argument_list|()
decl_stmt|;
name|Cell
name|lastKey
init|=
name|reader
operator|.
name|getLastKey
argument_list|()
decl_stmt|;
comment|// if the midkey is the same as the first or last keys, we cannot (ever) split this region.
if|if
condition|(
name|comparator
operator|.
name|compareRows
argument_list|(
name|midkey
argument_list|,
name|firstKey
argument_list|)
operator|==
literal|0
operator|||
name|comparator
operator|.
name|compareRows
argument_list|(
name|midkey
argument_list|,
name|lastKey
argument_list|)
operator|==
literal|0
condition|)
block|{
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
literal|"cannot split because midkey is the same as first or last row"
argument_list|)
expr_stmt|;
block|}
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
return|return
name|Optional
operator|.
name|of
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|midkey
argument_list|)
argument_list|)
return|;
block|}
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
end_class

end_unit

