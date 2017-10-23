begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  * */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|beans
operator|.
name|IntrospectionException
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
name|GarbageCollectorMXBean
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
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryPoolMXBean
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
name|RuntimeMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Hashtable
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
name|Set
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|InstanceNotFoundException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanAttributeInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanInfo
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

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ReflectionException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|openmbean
operator|.
name|CompositeData
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonProcessingException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|JsonNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
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
name|codehaus
operator|.
name|jettison
operator|.
name|json
operator|.
name|JSONException
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|JSONMetricUtil
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
name|JSONMetricUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|MBeanServer
name|mbServer
init|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
decl_stmt|;
comment|//MBeans ObjectName domain names
specifier|public
specifier|static
specifier|final
name|String
name|JAVA_LANG_DOMAIN
init|=
literal|"java.lang"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JAVA_NIO_DOMAIN
init|=
literal|"java.nio"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SUN_MGMT_DOMAIN
init|=
literal|"com.sun.management"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_DOMAIN
init|=
literal|"Hadoop"
decl_stmt|;
comment|//MBeans ObjectName properties key names
specifier|public
specifier|static
specifier|final
name|String
name|TYPE_KEY
init|=
literal|"type"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NAME_KEY
init|=
literal|"name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERVICE_KEY
init|=
literal|"service"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SUBSYSTEM_KEY
init|=
literal|"sub"
decl_stmt|;
comment|/**  * Utility for getting metric values. Collection of static methods intended for  * easier access to metric values.  */
specifier|private
name|JSONMetricUtil
parameter_list|()
block|{
comment|// Not to be called
block|}
specifier|public
specifier|static
name|MBeanAttributeInfo
index|[]
name|getMBeanAttributeInfo
parameter_list|(
name|ObjectName
name|bean
parameter_list|)
throws|throws
name|IntrospectionException
throws|,
name|InstanceNotFoundException
throws|,
name|ReflectionException
throws|,
name|IntrospectionException
throws|,
name|javax
operator|.
name|management
operator|.
name|IntrospectionException
block|{
name|MBeanInfo
name|mbinfo
init|=
name|mbServer
operator|.
name|getMBeanInfo
argument_list|(
name|bean
argument_list|)
decl_stmt|;
return|return
name|mbinfo
operator|.
name|getAttributes
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Object
name|getValueFromMBean
parameter_list|(
name|ObjectName
name|bean
parameter_list|,
name|String
name|attribute
parameter_list|)
block|{
name|Object
name|value
init|=
literal|null
decl_stmt|;
try|try
block|{
name|value
operator|=
name|mbServer
operator|.
name|getAttribute
argument_list|(
name|bean
argument_list|,
name|attribute
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to get value from MBean= "
operator|+
name|bean
operator|.
name|toString
argument_list|()
operator|+
literal|"for attribute="
operator|+
name|attribute
operator|+
literal|" "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
comment|/**    * Returns a subset of mbeans defined by qry.    * Modeled after DumpRegionServerMetrics#dumpMetrics.    * Example: String qry= "java.lang:type=Memory"    * @throws MalformedObjectNameException if json have bad format    * @throws IOException /    * @return String representation of json array.    */
specifier|public
specifier|static
name|String
name|dumpBeanToString
parameter_list|(
name|String
name|qry
parameter_list|)
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
name|qry
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
name|JsonNode
name|mappStringToJsonNode
parameter_list|(
name|String
name|jsonString
parameter_list|)
throws|throws
name|JsonProcessingException
throws|,
name|IOException
block|{
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|JsonNode
name|node
init|=
name|mapper
operator|.
name|readTree
argument_list|(
name|jsonString
argument_list|)
decl_stmt|;
return|return
name|node
return|;
block|}
specifier|public
specifier|static
name|JsonNode
name|searchJson
parameter_list|(
name|JsonNode
name|tree
parameter_list|,
name|String
name|searchKey
parameter_list|)
throws|throws
name|JsonProcessingException
throws|,
name|IOException
block|{
if|if
condition|(
name|tree
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|tree
operator|.
name|has
argument_list|(
name|searchKey
argument_list|)
condition|)
block|{
return|return
name|tree
operator|.
name|get
argument_list|(
name|searchKey
argument_list|)
return|;
block|}
if|if
condition|(
name|tree
operator|.
name|isContainerNode
argument_list|()
condition|)
block|{
for|for
control|(
name|JsonNode
name|branch
range|:
name|tree
control|)
block|{
name|JsonNode
name|branchResult
init|=
name|searchJson
argument_list|(
name|branch
argument_list|,
name|searchKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|branchResult
operator|!=
literal|null
operator|&&
operator|!
name|branchResult
operator|.
name|isMissingNode
argument_list|()
condition|)
block|{
return|return
name|branchResult
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Method for building hashtable used for constructing ObjectName.    * Mapping is done with arrays indices    * @param keys Hashtable keys    * @param values Hashtable values    * @return Hashtable or null if arrays are empty * or have different number of elements    */
specifier|public
specifier|static
name|Hashtable
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|buldKeyValueTable
parameter_list|(
name|String
index|[]
name|keys
parameter_list|,
name|String
index|[]
name|values
parameter_list|)
block|{
if|if
condition|(
name|keys
operator|.
name|length
operator|!=
name|values
operator|.
name|length
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"keys and values arrays must be same size"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|keys
operator|.
name|length
operator|==
literal|0
operator|||
name|values
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"keys and values arrays can not be empty;"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|Hashtable
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|table
init|=
operator|new
name|Hashtable
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|table
return|;
block|}
specifier|public
specifier|static
name|ObjectName
name|buildObjectName
parameter_list|(
name|String
name|pattern
parameter_list|)
throws|throws
name|MalformedObjectNameException
block|{
return|return
operator|new
name|ObjectName
argument_list|(
name|pattern
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ObjectName
name|buildObjectName
parameter_list|(
name|String
name|domain
parameter_list|,
name|Hashtable
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|keyValueTable
parameter_list|)
throws|throws
name|MalformedObjectNameException
block|{
return|return
operator|new
name|ObjectName
argument_list|(
name|domain
argument_list|,
name|keyValueTable
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Set
argument_list|<
name|ObjectName
argument_list|>
name|getRegistredMBeans
parameter_list|(
name|ObjectName
name|name
parameter_list|,
name|MBeanServer
name|mbs
parameter_list|)
block|{
return|return
name|mbs
operator|.
name|queryNames
argument_list|(
name|name
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getProcessPID
parameter_list|()
block|{
return|return
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|split
argument_list|(
literal|"@"
argument_list|)
index|[
literal|0
index|]
return|;
block|}
specifier|public
specifier|static
name|String
name|getCommmand
parameter_list|()
throws|throws
name|MalformedObjectNameException
throws|,
name|IOException
throws|,
name|JSONException
block|{
name|RuntimeMXBean
name|runtimeBean
init|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
decl_stmt|;
return|return
name|runtimeBean
operator|.
name|getSystemProperties
argument_list|()
operator|.
name|get
argument_list|(
literal|"sun.java.command"
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|GarbageCollectorMXBean
argument_list|>
name|getGcCollectorBeans
parameter_list|()
block|{
name|List
argument_list|<
name|GarbageCollectorMXBean
argument_list|>
name|gcBeans
init|=
name|ManagementFactory
operator|.
name|getGarbageCollectorMXBeans
argument_list|()
decl_stmt|;
return|return
name|gcBeans
return|;
block|}
specifier|public
specifier|static
name|long
name|getLastGcDuration
parameter_list|(
name|ObjectName
name|gcCollector
parameter_list|)
block|{
name|long
name|lastGcDuration
init|=
literal|0
decl_stmt|;
name|Object
name|lastGcInfo
init|=
name|getValueFromMBean
argument_list|(
name|gcCollector
argument_list|,
literal|"LastGcInfo"
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastGcInfo
operator|!=
literal|null
operator|&&
name|lastGcInfo
operator|instanceof
name|CompositeData
condition|)
block|{
name|CompositeData
name|cds
init|=
operator|(
name|CompositeData
operator|)
name|lastGcInfo
decl_stmt|;
name|lastGcDuration
operator|=
operator|(
name|long
operator|)
name|cds
operator|.
name|get
argument_list|(
literal|"duration"
argument_list|)
expr_stmt|;
block|}
return|return
name|lastGcDuration
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|MemoryPoolMXBean
argument_list|>
name|getMemoryPools
parameter_list|()
block|{
name|List
argument_list|<
name|MemoryPoolMXBean
argument_list|>
name|mPools
init|=
name|ManagementFactory
operator|.
name|getMemoryPoolMXBeans
argument_list|()
decl_stmt|;
return|return
name|mPools
return|;
block|}
specifier|public
specifier|static
name|float
name|calcPercentage
parameter_list|(
name|long
name|a
parameter_list|,
name|long
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|0
operator|||
name|b
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
operator|(
operator|(
name|float
operator|)
name|a
operator|/
operator|(
name|float
operator|)
name|b
operator|)
operator|*
literal|100
return|;
block|}
block|}
end_class

end_unit
