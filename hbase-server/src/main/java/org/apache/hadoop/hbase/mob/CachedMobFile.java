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
name|mob
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|BloomType
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
name|HStoreFile
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

begin_comment
comment|/**  * Cached mob file.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CachedMobFile
extends|extends
name|MobFile
implements|implements
name|Comparable
argument_list|<
name|CachedMobFile
argument_list|>
block|{
specifier|private
name|long
name|accessCount
decl_stmt|;
specifier|private
name|AtomicLong
name|referenceCount
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|CachedMobFile
parameter_list|(
name|HStoreFile
name|sf
parameter_list|)
block|{
name|super
argument_list|(
name|sf
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|CachedMobFile
name|create
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// XXX: primaryReplica is only used for constructing the key of block cache so it is not a
comment|// critical problem if we pass the wrong value, so here we always pass true. Need to fix later.
name|HStoreFile
name|sf
init|=
operator|new
name|HStoreFile
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
operator|new
name|CachedMobFile
argument_list|(
name|sf
argument_list|)
return|;
block|}
specifier|public
name|void
name|access
parameter_list|(
name|long
name|accessCount
parameter_list|)
block|{
name|this
operator|.
name|accessCount
operator|=
name|accessCount
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|CachedMobFile
name|that
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|accessCount
operator|==
name|that
operator|.
name|accessCount
condition|)
return|return
literal|0
return|;
return|return
name|this
operator|.
name|accessCount
operator|<
name|that
operator|.
name|accessCount
condition|?
literal|1
else|:
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|CachedMobFile
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|compareTo
argument_list|(
operator|(
name|CachedMobFile
operator|)
name|obj
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|accessCount
operator|^
operator|(
name|accessCount
operator|>>>
literal|32
operator|)
argument_list|)
return|;
block|}
comment|/**    * Opens the mob file if it's not opened yet and increases the reference.    * It's not thread-safe. Use MobFileCache.openFile() instead.    * The reader of the mob file is just opened when it's not opened no matter how many times    * this open() method is invoked.    * The reference is a counter that how many times this reader is referenced. When the    * reference is 0, this reader is closed.    */
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|open
argument_list|()
expr_stmt|;
name|referenceCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
comment|/**    * Decreases the reference of the underlying reader for the mob file.    * It's not thread-safe. Use MobFileCache.closeFile() instead.    * This underlying reader isn't closed until the reference is 0.    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|refs
init|=
name|referenceCount
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|refs
operator|==
literal|0
condition|)
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Gets the reference of the current mob file.    * Internal usage, currently it's for testing.    * @return The reference of the current mob file.    */
specifier|public
name|long
name|getReferenceCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|referenceCount
operator|.
name|longValue
argument_list|()
return|;
block|}
block|}
end_class

end_unit

