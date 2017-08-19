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
name|util
operator|.
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * HFile cleaner that uses the timestamp of the hfile to determine if it should be deleted. By  * default they are allowed to live for {@value #DEFAULT_TTL}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|TimeToLiveHFileCleaner
extends|extends
name|BaseHFileCleanerDelegate
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
name|TimeToLiveHFileCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TTL_CONF_KEY
init|=
literal|"hbase.master.hfilecleaner.ttl"
decl_stmt|;
comment|// default ttl = 5 minutes
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_TTL
init|=
literal|60000
operator|*
literal|5
decl_stmt|;
comment|// Configured time a hfile can be kept after it was moved to the archive
specifier|private
name|long
name|ttl
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|ttl
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|TTL_CONF_KEY
argument_list|,
name|DEFAULT_TTL
argument_list|)
expr_stmt|;
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|long
name|time
init|=
name|fStat
operator|.
name|getModificationTime
argument_list|()
decl_stmt|;
name|long
name|life
init|=
name|currentTime
operator|-
name|time
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"HFile life:"
operator|+
name|life
operator|+
literal|", ttl:"
operator|+
name|ttl
operator|+
literal|", current:"
operator|+
name|currentTime
operator|+
literal|", from: "
operator|+
name|time
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|life
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found a hfile ("
operator|+
name|fStat
operator|.
name|getPath
argument_list|()
operator|+
literal|") newer than current time ("
operator|+
name|currentTime
operator|+
literal|"< "
operator|+
name|time
operator|+
literal|"), probably a clock skew"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|life
operator|>
name|ttl
return|;
block|}
block|}
end_class

end_unit

