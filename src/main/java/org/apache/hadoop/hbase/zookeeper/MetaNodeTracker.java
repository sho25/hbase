begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
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
name|Abortable
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
name|catalog
operator|.
name|CatalogTracker
import|;
end_import

begin_comment
comment|/**  * Tracks the unassigned zookeeper node used by the META table.  *  * A callback is made into the passed {@link CatalogTracker} when  *<code>.META.</code> completes a new assignment.  *<p>  * If META is already assigned when instantiating this class, you will not  * receive any notification for that assignment.  You will receive a  * notification after META has been successfully assigned to a new location.  */
end_comment

begin_class
specifier|public
class|class
name|MetaNodeTracker
extends|extends
name|ZooKeeperNodeTracker
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
name|MetaNodeTracker
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Catalog tracker to notify when META has a new assignment completed. */
specifier|private
specifier|final
name|CatalogTracker
name|catalogTracker
decl_stmt|;
comment|/**    * Creates a meta node tracker.    * @param watcher    * @param abortable    */
specifier|public
name|MetaNodeTracker
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|watcher
parameter_list|,
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|,
specifier|final
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|assignmentZNode
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|this
operator|.
name|catalogTracker
operator|=
name|catalogTracker
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
operator|!
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
return|return;
name|LOG
operator|.
name|info
argument_list|(
literal|"Detected completed assignment of META, notifying catalog tracker"
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|catalogTracker
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Tried to reset META server location after seeing the "
operator|+
literal|"completion of a new META assignment but got an IOE"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

