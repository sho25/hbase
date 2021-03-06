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
name|master
operator|.
name|cleaner
package|;
end_package

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
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
import|;
end_import

begin_comment
comment|/**  * Base class for the log cleaning function inside the master. By default, three  * cleaners:<code>TimeToLiveLogCleaner</code>,  *<code>TimeToLiveProcedureWALCleaner</code> and  *<code>ReplicationLogCleaner</code> are called in order. So if other effects  * are needed, implement your own LogCleanerDelegate and add it to the  * configuration "hbase.master.logcleaner.plugins", which is a comma-separated  * list of fully qualified class names. LogsCleaner will add it to the chain.  *<p>  * HBase ships with LogsCleaner as the default implementation.  *<p>  * This interface extends Configurable, so setConf needs to be called once  * before using the cleaner. Since LogCleanerDelegates are created in  * LogsCleaner by reflection. Classes that implements this interface should  * provide a default constructor.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|BaseLogCleanerDelegate
extends|extends
name|BaseFileCleanerDelegate
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isFileDeletable
parameter_list|(
name|FileStatus
name|fStat
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

