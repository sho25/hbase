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
name|coprocessor
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Hashtable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Random
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
name|MBeanServerConnection
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectInstance
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
name|remote
operator|.
name|JMXConnector
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|remote
operator|.
name|JMXConnectorFactory
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
name|HBaseClassTestRule
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
name|HBaseTestingUtility
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
name|JMXListener
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
name|TableName
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
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|client
operator|.
name|ColumnFamilyDescriptorBuilder
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
name|client
operator|.
name|Get
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
name|client
operator|.
name|Put
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
name|client
operator|.
name|Table
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|testclassification
operator|.
name|CoprocessorTests
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
name|testclassification
operator|.
name|MediumTests
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
name|hamcrest
operator|.
name|CustomTypeSafeMatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|AllOf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMetaTableMetrics
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestMetaTableMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestMetaTableMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|NAME1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestExampleMetaTableMetricsOne"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ColumnFamilyDescriptor
name|CFD
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|value
init|=
literal|"foo"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|METRICS_ATTRIBUTE_NAME_PREFIX
init|=
literal|"MetaTable_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|METRICS_ATTRIBUTE_NAME_POSTFIXES
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"_count"
argument_list|,
literal|"_mean_rate"
argument_list|,
literal|"_1min_rate"
argument_list|,
literal|"_5min_rate"
argument_list|,
literal|"_15min_rate"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|connectorPort
init|=
literal|61120
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"info"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"any"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|tablename
decl_stmt|;
specifier|private
specifier|final
name|int
name|nthreads
init|=
literal|20
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Set system coprocessor so it can be applied to meta regions
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.region.classes"
argument_list|,
name|MetaTableMetrics
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|,
name|JMXListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
do|do
block|{
name|int
name|sign
init|=
name|i
operator|%
literal|2
operator|==
literal|0
condition|?
literal|1
else|:
operator|-
literal|1
decl_stmt|;
name|connectorPort
operator|+=
name|sign
operator|*
name|rand
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|HBaseTestingUtility
operator|.
name|available
argument_list|(
name|connectorPort
argument_list|)
condition|)
do|;
try|try
block|{
name|conf
operator|.
name|setInt
argument_list|(
literal|"regionserver.rmi.registry.port"
argument_list|,
name|connectorPort
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Encountered exception when starting cluster. Trying port {}"
argument_list|,
name|connectorPort
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
comment|// this is to avoid "IllegalStateException: A mini-cluster is already running"
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Encountered exception shutting down cluster"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|// Verifies that meta table metrics exist in jmx. In case of one table (one region) with a single
comment|// client: 9 metrics
comment|// are generated and for each metrics, there should be 5 JMX attributes produced. e.g. for one
comment|// table, there should
comment|// be 5 MetaTable_table_<TableName>_request attributes, such as:
comment|// - MetaTable_table_TestExampleMetaTableMetricsOne_request_count
comment|// - MetaTable_table_TestExampleMetaTableMetricsOne_request_mean_rate
comment|// - MetaTable_table_TestExampleMetaTableMetricsOne_request_1min_rate
comment|// - MetaTable_table_TestExampleMetaTableMetricsOne_request_5min_rate
comment|// - MetaTable_table_TestExampleMetaTableMetricsOne_request_15min_rate
annotation|@
name|Test
specifier|public
name|void
name|testMetaTableMetricsInJmx
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|NAME1
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|CFD
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|writeData
argument_list|(
name|NAME1
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|NAME1
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
literal|2000
argument_list|,
literal|true
argument_list|,
parameter_list|()
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|jmxMetrics
init|=
name|readMetaTableJmxMetrics
argument_list|()
decl_stmt|;
name|boolean
name|allMetricsFound
init|=
name|AllOf
operator|.
name|allOf
argument_list|(
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_get_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_put_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_delete_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_region_.+_lossy_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_table_"
operator|+
name|NAME1
operator|+
literal|"_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_client_.+_put_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_client_.+_get_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_client_.+_delete_request"
argument_list|)
argument_list|,
name|containsPositiveJmxAttributesFor
argument_list|(
literal|"MetaTable_client_.+_lossy_request"
argument_list|)
argument_list|)
operator|.
name|matches
argument_list|(
name|jmxMetrics
argument_list|)
decl_stmt|;
if|if
condition|(
name|allMetricsFound
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"all the meta table metrics found with positive values: {}"
argument_list|,
name|jmxMetrics
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"couldn't find all the meta table metrics with positive values: {}"
argument_list|,
name|jmxMetrics
argument_list|)
expr_stmt|;
block|}
return|return
name|allMetricsFound
return|;
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentAccess
parameter_list|()
block|{
try|try
block|{
name|tablename
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hbase:meta"
argument_list|)
expr_stmt|;
name|int
name|numRows
init|=
literal|3000
decl_stmt|;
name|int
name|numRowsInTableBefore
init|=
name|UTIL
operator|.
name|countRows
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tablename
argument_list|)
argument_list|)
decl_stmt|;
name|putData
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|int
name|numRowsInTableAfter
init|=
name|UTIL
operator|.
name|countRows
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tablename
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|numRowsInTableAfter
operator|>=
name|numRowsInTableBefore
operator|+
name|numRows
argument_list|)
expr_stmt|;
name|getData
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Caught InterruptedException while testConcurrentAccess: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|fail
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
name|info
argument_list|(
literal|"Caught IOException while testConcurrentAccess: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writeData
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|t
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|NUM_ROWS
argument_list|)
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
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Matcher
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
argument_list|>
name|containsPositiveJmxAttributesFor
parameter_list|(
specifier|final
name|String
name|regexp
parameter_list|)
block|{
return|return
operator|new
name|CustomTypeSafeMatcher
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
argument_list|>
argument_list|(
literal|"failed to find all the 5 positive JMX attributes for: "
operator|+
name|regexp
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|matchesSafely
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|values
parameter_list|)
block|{
for|for
control|(
name|String
name|key
range|:
name|values
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|metricsNamePostfix
range|:
name|METRICS_ATTRIBUTE_NAME_POSTFIXES
control|)
block|{
if|if
condition|(
name|key
operator|.
name|matches
argument_list|(
name|regexp
operator|+
name|metricsNamePostfix
argument_list|)
operator|&&
name|values
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
return|;
block|}
comment|/**    * Read the attributes from Hadoop->HBase->RegionServer->MetaTableMetrics in JMX    * @throws IOException when fails to retrieve jmx metrics.    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|readMetaTableJmxMetrics
parameter_list|()
throws|throws
name|IOException
block|{
name|JMXConnector
name|connector
init|=
literal|null
decl_stmt|;
name|ObjectName
name|target
init|=
literal|null
decl_stmt|;
name|MBeanServerConnection
name|mb
init|=
literal|null
decl_stmt|;
try|try
block|{
name|connector
operator|=
name|JMXConnectorFactory
operator|.
name|connect
argument_list|(
name|JMXListener
operator|.
name|buildJMXServiceURL
argument_list|(
name|connectorPort
argument_list|,
name|connectorPort
argument_list|)
argument_list|)
expr_stmt|;
name|mb
operator|=
name|connector
operator|.
name|getMBeanServerConnection
argument_list|()
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"JdkObsolete"
argument_list|)
name|Hashtable
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pairs
init|=
operator|new
name|Hashtable
argument_list|<>
argument_list|()
decl_stmt|;
name|pairs
operator|.
name|put
argument_list|(
literal|"service"
argument_list|,
literal|"HBase"
argument_list|)
expr_stmt|;
name|pairs
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"RegionServer"
argument_list|)
expr_stmt|;
name|pairs
operator|.
name|put
argument_list|(
literal|"sub"
argument_list|,
literal|"Coprocessor.Region.CP_org.apache.hadoop.hbase.coprocessor.MetaTableMetrics"
argument_list|)
expr_stmt|;
name|target
operator|=
operator|new
name|ObjectName
argument_list|(
literal|"Hadoop"
argument_list|,
name|pairs
argument_list|)
expr_stmt|;
name|MBeanInfo
name|beanInfo
init|=
name|mb
operator|.
name|getMBeanInfo
argument_list|(
name|target
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|existingAttrs
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|MBeanAttributeInfo
name|attrInfo
range|:
name|beanInfo
operator|.
name|getAttributes
argument_list|()
control|)
block|{
name|Object
name|value
init|=
name|mb
operator|.
name|getAttribute
argument_list|(
name|target
argument_list|,
name|attrInfo
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|attrInfo
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
name|METRICS_ATTRIBUTE_NAME_PREFIX
argument_list|)
operator|&&
name|value
operator|instanceof
name|Number
condition|)
block|{
name|existingAttrs
operator|.
name|put
argument_list|(
name|attrInfo
operator|.
name|getName
argument_list|()
argument_list|,
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"MBean Found: {}"
argument_list|,
name|target
argument_list|)
expr_stmt|;
return|return
name|existingAttrs
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get Meta Table Metrics bean (will retry later): {}"
argument_list|,
name|target
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|mb
operator|!=
literal|null
condition|)
block|{
name|Set
argument_list|<
name|ObjectInstance
argument_list|>
name|instances
init|=
name|mb
operator|.
name|queryMBeans
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|ObjectInstance
argument_list|>
name|iterator
init|=
name|instances
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"All the MBeans we found:"
argument_list|)
expr_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ObjectInstance
name|instance
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Class and object name: {} [{}]"
argument_list|,
name|instance
operator|.
name|getClassName
argument_list|()
argument_list|,
name|instance
operator|.
name|getObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|connector
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|connector
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
specifier|private
name|void
name|putData
parameter_list|(
name|int
name|nrows
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Putting {} rows in hbase:meta"
argument_list|,
name|nrows
argument_list|)
expr_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|nthreads
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|nthreads
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
operator|-
literal|1
index|]
operator|=
operator|new
name|PutThread
argument_list|(
literal|1
argument_list|,
name|nrows
argument_list|)
expr_stmt|;
block|}
name|startThreadsAndWaitToJoin
argument_list|(
name|threads
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getData
parameter_list|(
name|int
name|nrows
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Getting {} rows from hbase:meta"
argument_list|,
name|nrows
argument_list|)
expr_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|nthreads
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|nthreads
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
operator|-
literal|1
index|]
operator|=
operator|new
name|GetThread
argument_list|(
literal|1
argument_list|,
name|nrows
argument_list|)
expr_stmt|;
block|}
name|startThreadsAndWaitToJoin
argument_list|(
name|threads
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|startThreadsAndWaitToJoin
parameter_list|(
name|Thread
index|[]
name|threads
parameter_list|)
throws|throws
name|InterruptedException
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|nthreads
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
operator|-
literal|1
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|nthreads
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
operator|-
literal|1
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|PutThread
extends|extends
name|Thread
block|{
name|int
name|start
decl_stmt|;
name|int
name|end
decl_stmt|;
name|PutThread
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|end
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tablename
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<=
name|end
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"tableName,rowKey%d,region%d"
argument_list|,
name|i
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|col
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Value"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
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
literal|"Caught IOException while PutThread operation"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
class|class
name|GetThread
extends|extends
name|Thread
block|{
name|int
name|start
decl_stmt|;
name|int
name|end
decl_stmt|;
name|GetThread
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|end
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tablename
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<=
name|end
condition|;
name|i
operator|++
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"tableName,rowKey%d,region%d"
argument_list|,
name|i
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
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
literal|"Caught IOException while GetThread operation"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

