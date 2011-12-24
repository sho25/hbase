begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|hfile
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
name|nio
operator|.
name|ByteBuffer
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
name|StoreFile
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * Controls what kind of data block encoding is used. If data block encoding is  * not set, methods should just return unmodified block. All of the methods do  * something meaningful if BlockType is DATA_BLOCK or ENCODED_DATA. Otherwise  * they just return the unmodified block.  *<p>  * Read path: [parsed from disk] -> {@link #afterReadFromDisk(HFileBlock)} ->  * [caching] ->  * {@link #afterReadFromDiskAndPuttingInCache(HFileBlock, boolean)} -> [used  * somewhere]  *<p>  * where [caching] looks:  *<pre>  * ------------------------------------>  *   \----> {@link #beforeBlockCache(HFileBlock)}  *</pre>  *<p>  * Write path: [sorted KeyValues have been created] ->  * {@link #beforeWriteToDisk(ByteBuffer)} -> [(optional) compress] -> [write to  * disk]  *<p>  * Reading from cache path: [get from cache] ->  * {@link #afterBlockCache(HFileBlock, boolean)}  *<p>  * Storing data in file info: {@link #saveMetadata(StoreFile.Writer)}  *<p>  * Creating algorithm specific Scanner: {@link #useEncodedScanner()}  */
end_comment

begin_interface
specifier|public
interface|interface
name|HFileDataBlockEncoder
block|{
comment|/**    * Should be called after each HFileBlock of type DATA_BLOCK or    * ENCODED_DATA_BLOCK is read from disk, but before it is put into the cache.    * @param block Block read from HFile stored on disk.    * @return non null block which is coded according to the settings.    */
specifier|public
name|HFileBlock
name|afterReadFromDisk
parameter_list|(
name|HFileBlock
name|block
parameter_list|)
function_decl|;
comment|/**    * Should be called after each HFileBlock of type DATA_BLOCK or    * ENCODED_DATA_BLOCK is read from disk and after it is saved in cache    * @param block Block read from HFile stored on disk.    * @param isCompaction Will block be used for compaction.    * @return non null block which is coded according to the settings.    */
specifier|public
name|HFileBlock
name|afterReadFromDiskAndPuttingInCache
parameter_list|(
name|HFileBlock
name|block
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|includesMemsoreTS
parameter_list|)
function_decl|;
comment|/**    * Should be called before an encoded or unencoded data block is written to    * disk.    * @param in KeyValues next to each other    * @return a non-null on-heap buffer containing the contents of the    *         HFileBlock with unfilled header and block type    */
specifier|public
name|Pair
argument_list|<
name|ByteBuffer
argument_list|,
name|BlockType
argument_list|>
name|beforeWriteToDisk
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
function_decl|;
comment|/**    * Should always be called before putting a block into cache.    * @param block block that needs to be put into cache.    * @return the block to put into cache instead (possibly the same)    */
specifier|public
name|HFileBlock
name|beforeBlockCache
parameter_list|(
name|HFileBlock
name|block
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
function_decl|;
comment|/**    * After getting block from cache.    * @param block block which was returned from cache, may be null.    * @param isCompaction Will block be used for compaction.    * @param includesMemstoreTS whether we have a memstore timestamp encoded    *    as a variable-length integer after each key-value pair    * @return HFileBlock to use. Can be null, even if argument is not null.    */
specifier|public
name|HFileBlock
name|afterBlockCache
parameter_list|(
name|HFileBlock
name|block
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
function_decl|;
comment|/**    * Should special version of scanner be used.    * @param isCompaction Will scanner be used for compaction.    * @return Whether to use encoded scanner.    */
specifier|public
name|boolean
name|useEncodedScanner
parameter_list|(
name|boolean
name|isCompaction
parameter_list|)
function_decl|;
comment|/**    * Save metadata in StoreFile which will be written to disk    * @param storeFileWriter writer for a given StoreFile    * @exception IOException on disk problems    */
specifier|public
name|void
name|saveMetadata
parameter_list|(
name|StoreFile
operator|.
name|Writer
name|storeFileWriter
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

