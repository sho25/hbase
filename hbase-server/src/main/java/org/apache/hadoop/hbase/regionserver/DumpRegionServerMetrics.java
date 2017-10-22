begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|JSONBean
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
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanServer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MalformedObjectNameException
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
comment|/**  * Utility for doing JSON and MBeans.  */
end_comment

begin_class
specifier|public
class|class
name|DumpRegionServerMetrics
block|{
comment|/**    * Dump out a subset of regionserver mbeans only, not all of them, as json on System.out.    */
specifier|public
specifier|static
name|String
name|dumpMetrics
parameter_list|()
throws|throws
name|MalformedObjectNameException
throws|,
name|IOException
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|(
literal|1024
operator|*
literal|100
argument_list|)
decl_stmt|;
comment|// Guess this size
try|try
init|(
name|PrintWriter
name|writer
init|=
operator|new
name|PrintWriter
argument_list|(
name|sw
argument_list|)
init|)
block|{
name|JSONBean
name|dumper
init|=
operator|new
name|JSONBean
argument_list|()
decl_stmt|;
try|try
init|(
name|JSONBean
operator|.
name|Writer
name|jsonBeanWriter
init|=
name|dumper
operator|.
name|open
argument_list|(
name|writer
argument_list|)
init|)
block|{
name|MBeanServer
name|mbeanServer
init|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
decl_stmt|;
name|jsonBeanWriter
operator|.
name|write
argument_list|(
name|mbeanServer
argument_list|,
operator|new
name|ObjectName
argument_list|(
literal|"java.lang:type=Memory"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|jsonBeanWriter
operator|.
name|write
argument_list|(
name|mbeanServer
argument_list|,
operator|new
name|ObjectName
argument_list|(
literal|"Hadoop:service=HBase,name=RegionServer,sub=IPC"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|jsonBeanWriter
operator|.
name|write
argument_list|(
name|mbeanServer
argument_list|,
operator|new
name|ObjectName
argument_list|(
literal|"Hadoop:service=HBase,name=RegionServer,sub=Replication"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|jsonBeanWriter
operator|.
name|write
argument_list|(
name|mbeanServer
argument_list|,
operator|new
name|ObjectName
argument_list|(
literal|"Hadoop:service=HBase,name=RegionServer,sub=Server"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|sw
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|sw
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|MalformedObjectNameException
block|{
name|String
name|str
init|=
name|dumpMetrics
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|str
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

