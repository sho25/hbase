begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|visibility
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|exceptions
operator|.
name|DeserializationException
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|MultiUserAuthorizations
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|UserAuthorizations
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabel
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
name|hbase
operator|.
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Maintains the cache for visibility labels and also uses the zookeeper to update the labels in the  * system. The cache updation happens based on the data change event that happens on the zookeeper  * znode for labels table  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VisibilityLabelsCache
implements|implements
name|VisibilityLabelOrdinalProvider
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
name|VisibilityLabelsCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NON_EXIST_LABEL_ORDINAL
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|EMPTY_LIST
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
name|EMPTY_SET
init|=
name|Collections
operator|.
name|emptySet
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|VisibilityLabelsCache
name|instance
decl_stmt|;
specifier|private
name|ZKVisibilityLabelWatcher
name|zkVisibilityWatcher
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|labels
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|ordinalVsLabels
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|userAuths
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * This covers the members labels, ordinalVsLabels and userAuths    */
specifier|private
name|ReentrantReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
specifier|private
name|VisibilityLabelsCache
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|zkVisibilityWatcher
operator|=
operator|new
name|ZKVisibilityLabelWatcher
argument_list|(
name|watcher
argument_list|,
name|this
argument_list|,
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|zkVisibilityWatcher
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ZooKeeper initialization failed"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|ke
argument_list|)
throw|;
block|}
block|}
comment|/**    * Creates the singleton instance, if not yet present, and returns the same.    * @param watcher    * @param conf    * @return Singleton instance of VisibilityLabelsCache    * @throws IOException    */
specifier|public
specifier|synchronized
specifier|static
name|VisibilityLabelsCache
name|createAndGet
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// VisibilityLabelService#init() for different regions (in same RS) passes same instance of
comment|// watcher as all get the instance from RS.
comment|// watcher != instance.zkVisibilityWatcher.getWatcher() - This check is needed only in UTs with
comment|// RS restart. It will be same JVM in which RS restarts and instance will be not null. But the
comment|// watcher associated with existing instance will be stale as the restarted RS will have new
comment|// watcher with it.
if|if
condition|(
name|instance
operator|==
literal|null
operator|||
name|watcher
operator|!=
name|instance
operator|.
name|zkVisibilityWatcher
operator|.
name|getWatcher
argument_list|()
condition|)
block|{
name|instance
operator|=
operator|new
name|VisibilityLabelsCache
argument_list|(
name|watcher
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
comment|/**    * @return Singleton instance of VisibilityLabelsCache    * @throws IllegalStateException    *           when this is called before calling    *           {@link #createAndGet(ZooKeeperWatcher, Configuration)}    */
specifier|public
specifier|static
name|VisibilityLabelsCache
name|get
parameter_list|()
block|{
comment|// By the time this method is called, the singleton instance of VisibilityLabelsCache should
comment|// have been created.
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"VisibilityLabelsCache not yet instantiated"
argument_list|)
throw|;
block|}
return|return
name|instance
return|;
block|}
specifier|public
name|void
name|refreshLabelsCache
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|VisibilityLabel
argument_list|>
name|visibilityLabels
init|=
literal|null
decl_stmt|;
try|try
block|{
name|visibilityLabels
operator|=
name|VisibilityUtils
operator|.
name|readLabelsFromZKData
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|dse
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|dse
argument_list|)
throw|;
block|}
name|this
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|labels
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ordinalVsLabels
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|VisibilityLabel
name|visLabel
range|:
name|visibilityLabels
control|)
block|{
name|String
name|label
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|visLabel
operator|.
name|getLabel
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|labels
operator|.
name|put
argument_list|(
name|label
argument_list|,
name|visLabel
operator|.
name|getOrdinal
argument_list|()
argument_list|)
expr_stmt|;
name|ordinalVsLabels
operator|.
name|put
argument_list|(
name|visLabel
operator|.
name|getOrdinal
argument_list|()
argument_list|,
name|label
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|refreshUserAuthsCache
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|MultiUserAuthorizations
name|multiUserAuths
init|=
literal|null
decl_stmt|;
try|try
block|{
name|multiUserAuths
operator|=
name|VisibilityUtils
operator|.
name|readUserAuthsFromZKData
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|dse
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|dse
argument_list|)
throw|;
block|}
name|this
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|userAuths
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|UserAuthorizations
name|userAuths
range|:
name|multiUserAuths
operator|.
name|getUserAuthsList
argument_list|()
control|)
block|{
name|String
name|user
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|userAuths
operator|.
name|getUser
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|userAuths
operator|.
name|put
argument_list|(
name|user
argument_list|,
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|userAuths
operator|.
name|getAuthList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @param label Not null label string    * @return The ordinal for the label. The ordinal starts from 1. Returns 0 when passed a non    *         existing label.    */
annotation|@
name|Override
specifier|public
name|int
name|getLabelOrdinal
parameter_list|(
name|String
name|label
parameter_list|)
block|{
name|Integer
name|ordinal
init|=
literal|null
decl_stmt|;
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|ordinal
operator|=
name|labels
operator|.
name|get
argument_list|(
name|label
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|ordinal
operator|!=
literal|null
condition|)
block|{
return|return
name|ordinal
operator|.
name|intValue
argument_list|()
return|;
block|}
comment|// 0 denotes not available
return|return
name|NON_EXIST_LABEL_ORDINAL
return|;
block|}
comment|/**    * @param ordinal The ordinal of label which we are looking for.    * @return The label having the given ordinal. Returns<code>null</code> when no label exist in    *         the system with given ordinal    */
specifier|public
name|String
name|getLabel
parameter_list|(
name|int
name|ordinal
parameter_list|)
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|this
operator|.
name|ordinalVsLabels
operator|.
name|get
argument_list|(
name|ordinal
argument_list|)
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @return The total number of visibility labels.    */
specifier|public
name|int
name|getLabelsCount
parameter_list|()
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|this
operator|.
name|labels
operator|.
name|size
argument_list|()
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAuths
parameter_list|(
name|String
name|user
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|auths
init|=
name|EMPTY_LIST
decl_stmt|;
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|authOrdinals
init|=
name|userAuths
operator|.
name|get
argument_list|(
name|user
argument_list|)
decl_stmt|;
if|if
condition|(
name|authOrdinals
operator|!=
literal|null
condition|)
block|{
name|auths
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|authOrdinals
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|authOrdinal
range|:
name|authOrdinals
control|)
block|{
name|auths
operator|.
name|add
argument_list|(
name|ordinalVsLabels
operator|.
name|get
argument_list|(
name|authOrdinal
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|auths
return|;
block|}
comment|/**    * Returns the list of ordinals of authentications associated with the user    *    * @param user Not null value.    * @return the list of ordinals    */
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|getAuthsAsOrdinals
parameter_list|(
name|String
name|user
parameter_list|)
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|auths
init|=
name|userAuths
operator|.
name|get
argument_list|(
name|user
argument_list|)
decl_stmt|;
return|return
operator|(
name|auths
operator|==
literal|null
operator|)
condition|?
name|EMPTY_SET
else|:
name|auths
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|writeToZookeeper
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|boolean
name|labelsOrUserAuths
parameter_list|)
block|{
name|this
operator|.
name|zkVisibilityWatcher
operator|.
name|writeToZookeeper
argument_list|(
name|data
argument_list|,
name|labelsOrUserAuths
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

