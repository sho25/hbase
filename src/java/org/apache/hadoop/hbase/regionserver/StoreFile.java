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
package|;
end_package

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
name|HalfHFileReader
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
name|Reference
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
name|hfile
operator|.
name|BlockCache
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
name|hfile
operator|.
name|Compression
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
name|hfile
operator|.
name|HFile
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
name|hfile
operator|.
name|LruBlockCache
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
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * A Store data file.  Stores usually have one or more of these files.  They  * are produced by flushing the memcache to disk.  To  * create, call {@link #getWriter(FileSystem, Path)} and append data.  Be  * sure to add any metadata before calling close on the Writer  * (Use the appendMetadata convenience methods). On close, a StoreFile is  * sitting in the Filesystem.  To refer to it, create a StoreFile instance  * passing filesystem and path.  To read, call {@link #getReader()}.  *<p>StoreFiles may also reference store files in another Store.  */
end_comment

begin_class
specifier|public
class|class
name|StoreFile
implements|implements
name|HConstants
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|StoreFile
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HFILE_CACHE_SIZE_KEY
init|=
literal|"hfile.block.cache.size"
decl_stmt|;
specifier|private
specifier|static
name|BlockCache
name|hfileBlockCache
init|=
literal|null
decl_stmt|;
comment|// Make default block size for StoreFiles 8k while testing.  TODO: FIX!
comment|// Need to make it 8k for testing.
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_BLOCKSIZE_SMALL
init|=
literal|8
operator|*
literal|1024
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
comment|// This file's path.
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
comment|// If this storefile references another, this is the reference instance.
specifier|private
name|Reference
name|reference
decl_stmt|;
comment|// If this StoreFile references another, this is the other files path.
specifier|private
name|Path
name|referencePath
decl_stmt|;
comment|// Should the block cache be used or not.
specifier|private
name|boolean
name|blockcache
decl_stmt|;
comment|// Keys for metadata stored in backing HFile.
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|MAX_SEQ_ID_KEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"MAX_SEQ_ID_KEY"
argument_list|)
decl_stmt|;
comment|// Set when we obtain a Reader.
specifier|private
name|long
name|sequenceid
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|MAJOR_COMPACTION_KEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"MAJOR_COMPACTION_KEY"
argument_list|)
decl_stmt|;
comment|// If true, this file was product of a major compaction.  Its then set
comment|// whenever you get a Reader.
specifier|private
name|AtomicBoolean
name|majorCompaction
init|=
literal|null
decl_stmt|;
comment|/*    * Regex that will work for straight filenames and for reference names.    * If reference, then the regex has more than just one group.  Group 1 is    * this files id.  Group 2 the referenced region name, etc.    */
specifier|private
specifier|static
specifier|final
name|Pattern
name|REF_NAME_PARSER
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^(\\d+)(?:\\.(.+))?$"
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|HFile
operator|.
name|Reader
name|reader
decl_stmt|;
comment|// Used making file ids.
specifier|private
specifier|final
specifier|static
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HBaseConfiguration
name|conf
decl_stmt|;
comment|/**    * Constructor, loads a reader and it's indices, etc. May allocate a     * substantial amount of ram depending on the underlying files (10-20MB?).    *     * @param fs  The current file system to use.    * @param p  The path of the file.    * @param blockcache<code>true</code> if the block cache is enabled.    * @param conf  The current configuration.    * @throws IOException When opening the reader fails.    */
name|StoreFile
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|boolean
name|blockcache
parameter_list|,
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|p
expr_stmt|;
name|this
operator|.
name|blockcache
operator|=
name|blockcache
expr_stmt|;
if|if
condition|(
name|isReference
argument_list|(
name|p
argument_list|)
condition|)
block|{
name|this
operator|.
name|reference
operator|=
name|Reference
operator|.
name|read
argument_list|(
name|fs
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|this
operator|.
name|referencePath
operator|=
name|getReferredToFile
argument_list|(
name|this
operator|.
name|path
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|reader
operator|=
name|open
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return Path or null if this StoreFile was made with a Stream.    */
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|path
return|;
block|}
comment|/**    * @return The Store/ColumnFamily this file belongs to.    */
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|this
operator|.
name|path
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return True if this is a StoreFile Reference; call after {@link #open()}    * else may get wrong answer.    */
name|boolean
name|isReference
parameter_list|()
block|{
return|return
name|this
operator|.
name|reference
operator|!=
literal|null
return|;
block|}
comment|/**    * @param p Path to check.    * @return True if the path has format of a HStoreFile reference.    */
specifier|public
specifier|static
name|boolean
name|isReference
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|)
block|{
return|return
name|isReference
argument_list|(
name|p
argument_list|,
name|REF_NAME_PARSER
operator|.
name|matcher
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param p Path to check.    * @param m Matcher to use.    * @return True if the path has format of a HStoreFile reference.    */
specifier|public
specifier|static
name|boolean
name|isReference
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|Matcher
name|m
parameter_list|)
block|{
if|if
condition|(
name|m
operator|==
literal|null
operator|||
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed match of store file name "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed match of store file name "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|m
operator|.
name|groupCount
argument_list|()
operator|>
literal|1
operator|&&
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/*    * Return path to the file referred to by a Reference.  Presumes a directory    * hierarchy of<code>${hbase.rootdir}/tablename/regionname/familyname</code>.    * @param p Path to a Reference file.    * @return Calculated path to parent region file.    * @throws IOException    */
specifier|static
name|Path
name|getReferredToFile
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|REF_NAME_PARSER
operator|.
name|matcher
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
operator|||
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed match of store file name "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed match of store file name "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
comment|// Other region name is suffix on the passed Reference file name
name|String
name|otherRegion
init|=
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
decl_stmt|;
comment|// Tabledir is up two directories from where Reference was written.
name|Path
name|tableDir
init|=
name|p
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|String
name|nameStrippedOfSuffix
init|=
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// Build up new path with the referenced region in place of our current
comment|// region in the reference path.  Also strip regionname suffix from name.
return|return
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|otherRegion
argument_list|)
argument_list|,
name|p
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|nameStrippedOfSuffix
argument_list|)
return|;
block|}
comment|/**    * @return True if this file was made by a major compaction.    */
name|boolean
name|isMajorCompaction
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|majorCompaction
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"This has not been set yet"
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|majorCompaction
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @return This files maximum edit sequence id.    */
specifier|public
name|long
name|getMaxSequenceId
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|sequenceid
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalAccessError
argument_list|(
literal|"Has not been initialized"
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|sequenceid
return|;
block|}
comment|/**    * Returns the block cache or<code>null</code> in case none should be used.    *     * @param conf  The current configuration.    * @return The block cache or<code>null</code>.    */
specifier|public
specifier|static
specifier|synchronized
name|BlockCache
name|getBlockCache
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|hfileBlockCache
operator|!=
literal|null
condition|)
return|return
name|hfileBlockCache
return|;
name|long
name|cacheSize
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|HFILE_CACHE_SIZE_KEY
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
comment|// There should be a better way to optimize this. But oh well.
if|if
condition|(
name|cacheSize
operator|==
literal|0L
condition|)
return|return
literal|null
return|;
name|hfileBlockCache
operator|=
operator|new
name|LruBlockCache
argument_list|(
name|cacheSize
argument_list|)
expr_stmt|;
return|return
name|hfileBlockCache
return|;
block|}
comment|/**    * @return the blockcache    */
specifier|public
name|BlockCache
name|getBlockCache
parameter_list|()
block|{
return|return
name|blockcache
condition|?
name|getBlockCache
argument_list|(
name|conf
argument_list|)
else|:
literal|null
return|;
block|}
comment|/**    * Opens reader on this store file.  Called by Constructor.    * @return Reader for the store file.    * @throws IOException    * @see #close()    */
specifier|protected
name|HFile
operator|.
name|Reader
name|open
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|reader
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalAccessError
argument_list|(
literal|"Already open"
argument_list|)
throw|;
block|}
if|if
condition|(
name|isReference
argument_list|()
condition|)
block|{
name|this
operator|.
name|reader
operator|=
operator|new
name|HalfHFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|referencePath
argument_list|,
name|getBlockCache
argument_list|()
argument_list|,
name|this
operator|.
name|reference
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|reader
operator|=
operator|new
name|StoreFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|path
argument_list|,
name|getBlockCache
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Load up indices and fileinfo.
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|map
init|=
name|this
operator|.
name|reader
operator|.
name|loadFileInfo
argument_list|()
decl_stmt|;
comment|// Read in our metadata.
name|byte
index|[]
name|b
init|=
name|map
operator|.
name|get
argument_list|(
name|MAX_SEQ_ID_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
operator|!=
literal|null
condition|)
block|{
comment|// By convention, if halfhfile, top half has a sequence number> bottom
comment|// half. Thats why we add one in below. Its done for case the two halves
comment|// are ever merged back together --rare.  Without it, on open of store,
comment|// since store files are distingushed by sequence id, the one half would
comment|// subsume the other.
name|this
operator|.
name|sequenceid
operator|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|b
argument_list|)
expr_stmt|;
if|if
condition|(
name|isReference
argument_list|()
condition|)
block|{
if|if
condition|(
name|Reference
operator|.
name|isTopFileRegion
argument_list|(
name|this
operator|.
name|reference
operator|.
name|getFileRegion
argument_list|()
argument_list|)
condition|)
block|{
name|this
operator|.
name|sequenceid
operator|+=
literal|1
expr_stmt|;
block|}
block|}
block|}
name|b
operator|=
name|map
operator|.
name|get
argument_list|(
name|MAJOR_COMPACTION_KEY
argument_list|)
expr_stmt|;
if|if
condition|(
name|b
operator|!=
literal|null
condition|)
block|{
name|boolean
name|mc
init|=
name|Bytes
operator|.
name|toBoolean
argument_list|(
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|majorCompaction
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|majorCompaction
operator|=
operator|new
name|AtomicBoolean
argument_list|(
name|mc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|majorCompaction
operator|.
name|set
argument_list|(
name|mc
argument_list|)
expr_stmt|;
block|}
block|}
comment|// TODO read in bloom filter here, ignore if the column family config says
comment|// "no bloom filter" even if there is one in the hfile.
return|return
name|this
operator|.
name|reader
return|;
block|}
comment|/**    * Override to add some customization on HFile.Reader    */
specifier|static
class|class
name|StoreFileReader
extends|extends
name|HFile
operator|.
name|Reader
block|{
comment|/**      *       * @param fs      * @param path      * @param cache      * @throws IOException      */
specifier|public
name|StoreFileReader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|BlockCache
name|cache
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|cache
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|toStringFirstKey
parameter_list|()
block|{
return|return
name|KeyValue
operator|.
name|keyToString
argument_list|(
name|getFirstKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|toStringLastKey
parameter_list|()
block|{
return|return
name|KeyValue
operator|.
name|keyToString
argument_list|(
name|getLastKey
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * Override to add some customization on HalfHFileReader.    */
specifier|static
class|class
name|HalfStoreFileReader
extends|extends
name|HalfHFileReader
block|{
comment|/**      *       * @param fs      * @param p      * @param c      * @param r      * @throws IOException      */
specifier|public
name|HalfStoreFileReader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|p
parameter_list|,
name|BlockCache
name|c
parameter_list|,
name|Reference
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
name|c
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
operator|(
name|isTop
argument_list|()
condition|?
literal|", half=top"
else|:
literal|", half=bottom"
operator|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|toStringFirstKey
parameter_list|()
block|{
return|return
name|KeyValue
operator|.
name|keyToString
argument_list|(
name|getFirstKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|toStringLastKey
parameter_list|()
block|{
return|return
name|KeyValue
operator|.
name|keyToString
argument_list|(
name|getLastKey
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * @return Current reader.  Must call open first.    */
specifier|public
name|HFile
operator|.
name|Reader
name|getReader
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|reader
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalAccessError
argument_list|(
literal|"Call open first"
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|reader
return|;
block|}
comment|/**    * @throws IOException    */
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|reader
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|reader
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|path
operator|.
name|toString
argument_list|()
operator|+
operator|(
name|isReference
argument_list|()
condition|?
literal|"-"
operator|+
name|this
operator|.
name|referencePath
operator|+
literal|"-"
operator|+
name|reference
operator|.
name|toString
argument_list|()
else|:
literal|""
operator|)
return|;
block|}
comment|/**    * Delete this file    * @throws IOException     */
specifier|public
name|void
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Utility to help with rename.    * @param fs    * @param src    * @param tgt    * @return True if succeeded.    * @throws IOException    */
specifier|public
specifier|static
name|Path
name|rename
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|src
parameter_list|,
specifier|final
name|Path
name|tgt
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|src
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|src
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|src
argument_list|,
name|tgt
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed rename of "
operator|+
name|src
operator|+
literal|" to "
operator|+
name|tgt
argument_list|)
throw|;
block|}
return|return
name|tgt
return|;
block|}
comment|/**    * Get a store file writer. Client is responsible for closing file when done.    * If metadata, add BEFORE closing using    * {@link #appendMetadata(org.apache.hadoop.hbase.io.hfile.HFile.Writer, long)}.    * @param fs    * @param dir Path to family directory.  Makes the directory if doesn't exist.    * Creates a file with a unique name in this directory.    * @return HFile.Writer    * @throws IOException    */
specifier|public
specifier|static
name|HFile
operator|.
name|Writer
name|getWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getWriter
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|DEFAULT_BLOCKSIZE_SMALL
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Get a store file writer. Client is responsible for closing file when done.    * If metadata, add BEFORE closing using    * {@link #appendMetadata(org.apache.hadoop.hbase.io.hfile.HFile.Writer, long)}.    * @param fs    * @param dir Path to family directory.  Makes the directory if doesn't exist.    * Creates a file with a unique name in this directory.    * @param blocksize    * @param algorithm Pass null to get default.    * @param c Pass null to get default.    * @return HFile.Writer    * @throws IOException    */
specifier|public
specifier|static
name|HFile
operator|.
name|Writer
name|getWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|,
specifier|final
name|int
name|blocksize
parameter_list|,
specifier|final
name|Compression
operator|.
name|Algorithm
name|algorithm
parameter_list|,
specifier|final
name|KeyValue
operator|.
name|KeyComparator
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|dir
argument_list|)
expr_stmt|;
block|}
name|Path
name|path
init|=
name|getUniqueFile
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|)
decl_stmt|;
return|return
operator|new
name|HFile
operator|.
name|Writer
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|blocksize
argument_list|,
name|algorithm
operator|==
literal|null
condition|?
name|HFile
operator|.
name|DEFAULT_COMPRESSION_ALGORITHM
else|:
name|algorithm
argument_list|,
name|c
operator|==
literal|null
condition|?
name|KeyValue
operator|.
name|KEY_COMPARATOR
else|:
name|c
argument_list|)
return|;
block|}
comment|/**    * @param fs    * @param p    * @return random filename inside passed<code>dir</code>    */
specifier|static
name|Path
name|getUniqueFile
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|isDir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expecting a directory"
argument_list|)
throw|;
block|}
return|return
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|isDir
argument_list|()
condition|?
name|getRandomFilename
argument_list|(
name|fs
argument_list|,
name|p
argument_list|)
else|:
name|p
return|;
block|}
comment|/**    *    * @param fs    * @param dir    * @return Path to a file that doesn't exist at time of this invocation.    * @throws IOException    */
specifier|static
name|Path
name|getRandomFilename
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRandomFilename
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    *    * @param fs    * @param dir    * @param suffix    * @return Path to a file that doesn't exist at time of this invocation.    * @throws IOException    */
specifier|static
name|Path
name|getRandomFilename
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|,
specifier|final
name|String
name|suffix
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|id
init|=
operator|-
literal|1
decl_stmt|;
name|Path
name|p
init|=
literal|null
decl_stmt|;
do|do
block|{
name|id
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|id
argument_list|)
operator|+
operator|(
operator|(
name|suffix
operator|==
literal|null
operator|||
name|suffix
operator|.
name|length
argument_list|()
operator|<=
literal|0
operator|)
condition|?
literal|""
else|:
name|suffix
operator|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
do|;
return|return
name|p
return|;
block|}
comment|/**    * Write file metadata.    * Call before you call close on the passed<code>w</code> since its written    * as metadata to that file.    *     * @param w hfile writer    * @param maxSequenceId Maximum sequence id.    * @throws IOException    */
specifier|static
name|void
name|appendMetadata
parameter_list|(
specifier|final
name|HFile
operator|.
name|Writer
name|w
parameter_list|,
specifier|final
name|long
name|maxSequenceId
parameter_list|)
throws|throws
name|IOException
block|{
name|appendMetadata
argument_list|(
name|w
argument_list|,
name|maxSequenceId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes metadata.    * Call before you call close on the passed<code>w</code> since its written    * as metadata to that file.    * @param maxSequenceId Maximum sequence id.    * @param mc True if this file is product of a major compaction    * @throws IOException    */
specifier|static
name|void
name|appendMetadata
parameter_list|(
specifier|final
name|HFile
operator|.
name|Writer
name|w
parameter_list|,
specifier|final
name|long
name|maxSequenceId
parameter_list|,
specifier|final
name|boolean
name|mc
parameter_list|)
throws|throws
name|IOException
block|{
name|w
operator|.
name|appendFileInfo
argument_list|(
name|MAX_SEQ_ID_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|maxSequenceId
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|appendFileInfo
argument_list|(
name|MAJOR_COMPACTION_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|mc
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*    * Write out a split reference.    * @param fs    * @param splitDir Presumes path format is actually    *<code>SOME_DIRECTORY/REGIONNAME/FAMILY</code>.    * @param f File to split.    * @param splitRow    * @param range    * @return Path to created reference.    * @throws IOException    */
specifier|static
name|Path
name|split
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|splitDir
parameter_list|,
specifier|final
name|StoreFile
name|f
parameter_list|,
specifier|final
name|byte
index|[]
name|splitRow
parameter_list|,
specifier|final
name|Reference
operator|.
name|Range
name|range
parameter_list|)
throws|throws
name|IOException
block|{
comment|// A reference to the bottom half of the hsf store file.
name|Reference
name|r
init|=
operator|new
name|Reference
argument_list|(
name|splitRow
argument_list|,
name|range
argument_list|)
decl_stmt|;
comment|// Add the referred-to regions name as a dot separated suffix.
comment|// See REF_NAME_PARSER regex above.  The referred-to regions name is
comment|// up in the path of the passed in<code>f</code> -- parentdir is family,
comment|// then the directory above is the region name.
name|String
name|parentRegionName
init|=
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// Write reference with same file id only with the other region name as
comment|// suffix and into the new region location (under same family).
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|splitDir
argument_list|,
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|parentRegionName
argument_list|)
decl_stmt|;
return|return
name|r
operator|.
name|write
argument_list|(
name|fs
argument_list|,
name|p
argument_list|)
return|;
block|}
block|}
end_class

end_unit

