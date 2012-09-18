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
name|metrics
package|;
end_package

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
name|hbase
operator|.
name|metrics
operator|.
name|MetricsMBeanBase
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
name|metrics
operator|.
name|MetricsContext
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
name|metrics
operator|.
name|MetricsRecord
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
name|metrics
operator|.
name|MetricsUtil
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
name|metrics
operator|.
name|util
operator|.
name|MBeanUtil
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
name|metrics
operator|.
name|util
operator|.
name|MetricsRegistry
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectName
import|;
end_import

begin_comment
comment|/**  * Exports HBase system information as an MBean for JMX observation.  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBaseInfo
block|{
specifier|protected
specifier|static
class|class
name|HBaseInfoMBean
extends|extends
name|MetricsMBeanBase
block|{
specifier|private
specifier|final
name|ObjectName
name|mbeanName
decl_stmt|;
specifier|public
name|HBaseInfoMBean
parameter_list|(
name|MetricsRegistry
name|registry
parameter_list|,
name|String
name|rsName
parameter_list|)
block|{
name|super
argument_list|(
name|registry
argument_list|,
literal|"HBase cluster information"
argument_list|)
expr_stmt|;
comment|// The name seems wrong to me; should include clusterid IMO.
comment|// That would make it harder to locate and rare we have
comment|// two clusters up on single machine. St.Ack 20120309
name|mbeanName
operator|=
name|MBeanUtil
operator|.
name|registerMBean
argument_list|(
literal|"HBase"
argument_list|,
literal|"Info"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|mbeanName
operator|!=
literal|null
condition|)
name|MBeanUtil
operator|.
name|unregisterMBean
argument_list|(
name|mbeanName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|final
name|MetricsRecord
name|mr
decl_stmt|;
specifier|protected
specifier|final
name|HBaseInfoMBean
name|mbean
decl_stmt|;
specifier|protected
name|MetricsRegistry
name|registry
init|=
operator|new
name|MetricsRegistry
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HBaseInfo
name|theInstance
init|=
literal|null
decl_stmt|;
specifier|public
specifier|synchronized
specifier|static
name|HBaseInfo
name|init
parameter_list|()
block|{
if|if
condition|(
name|theInstance
operator|==
literal|null
condition|)
block|{
name|theInstance
operator|=
operator|new
name|HBaseInfo
argument_list|()
expr_stmt|;
block|}
return|return
name|theInstance
return|;
block|}
block|{
comment|// HBase jar info
operator|new
name|MetricsString
argument_list|(
literal|"date"
argument_list|,
name|registry
argument_list|,
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
name|VersionInfo
operator|.
name|getDate
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"revision"
argument_list|,
name|registry
argument_list|,
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
name|VersionInfo
operator|.
name|getRevision
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"url"
argument_list|,
name|registry
argument_list|,
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
name|VersionInfo
operator|.
name|getUrl
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"user"
argument_list|,
name|registry
argument_list|,
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
name|VersionInfo
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"version"
argument_list|,
name|registry
argument_list|,
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
name|VersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
comment|// Info on the HDFS jar that HBase has (aka: HDFS Client)
operator|new
name|MetricsString
argument_list|(
literal|"hdfsDate"
argument_list|,
name|registry
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getDate
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"hdfsRevision"
argument_list|,
name|registry
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getRevision
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"hdfsUrl"
argument_list|,
name|registry
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getUrl
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"hdfsUser"
argument_list|,
name|registry
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|MetricsString
argument_list|(
literal|"hdfsVersion"
argument_list|,
name|registry
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HBaseInfo
parameter_list|()
block|{
name|MetricsContext
name|context
init|=
name|MetricsUtil
operator|.
name|getContext
argument_list|(
literal|"hbase"
argument_list|)
decl_stmt|;
name|mr
operator|=
name|MetricsUtil
operator|.
name|createRecord
argument_list|(
name|context
argument_list|,
literal|"info"
argument_list|)
expr_stmt|;
name|String
name|name
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|mr
operator|.
name|setTag
argument_list|(
literal|"Info"
argument_list|,
name|name
argument_list|)
expr_stmt|;
comment|// export for JMX
name|mbean
operator|=
operator|new
name|HBaseInfoMBean
argument_list|(
name|this
operator|.
name|registry
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

