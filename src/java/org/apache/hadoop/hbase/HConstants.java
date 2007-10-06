begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * HConstants holds a bunch of HBase-related constants  */
end_comment

begin_interface
specifier|public
interface|interface
name|HConstants
block|{
comment|// Configuration parameters
comment|// TODO: URL for hbase master like hdfs URLs with host and port.
comment|// Like jdbc URLs?  URLs could be used to refer to table cells?
comment|// jdbc:mysql://[host][,failoverhost...][:port]/[database]
comment|// jdbc:mysql://[host][,failoverhost...][:port]/[database][?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
comment|// Key into HBaseConfiguration for the hbase.master address.
comment|// TODO: Support 'local': i.e. default of all running in single
comment|// process.  Same for regionserver. TODO: Is having HBase homed
comment|// on port 60k OK?
comment|/** Parameter name for master address */
specifier|static
specifier|final
name|String
name|MASTER_ADDRESS
init|=
literal|"hbase.master"
decl_stmt|;
comment|/** default host address */
specifier|static
specifier|final
name|String
name|DEFAULT_HOST
init|=
literal|"0.0.0.0"
decl_stmt|;
comment|/** Default master address */
specifier|static
specifier|final
name|String
name|DEFAULT_MASTER_ADDRESS
init|=
name|DEFAULT_HOST
operator|+
literal|":60000"
decl_stmt|;
specifier|static
specifier|final
name|int
name|DEFAULT_MASTER_INFOPORT
init|=
literal|60010
decl_stmt|;
comment|/** Parameter name for hbase.regionserver address. */
specifier|static
specifier|final
name|String
name|REGIONSERVER_ADDRESS
init|=
literal|"hbase.regionserver"
decl_stmt|;
comment|/** Default region server address */
specifier|static
specifier|final
name|String
name|DEFAULT_REGIONSERVER_ADDRESS
init|=
name|DEFAULT_HOST
operator|+
literal|":60020"
decl_stmt|;
specifier|static
specifier|final
name|int
name|DEFAULT_REGIONSERVER_INFOPORT
init|=
literal|60030
decl_stmt|;
comment|/** Parameter name for what region server interface to use. */
specifier|static
specifier|final
name|String
name|REGION_SERVER_CLASS
init|=
literal|"hbase.regionserver.class"
decl_stmt|;
comment|/** Default region server interface class name. */
specifier|static
specifier|final
name|String
name|DEFAULT_REGION_SERVER_CLASS
init|=
name|HRegionInterface
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|/** Parameter name for how often threads should wake up */
specifier|static
specifier|final
name|String
name|THREAD_WAKE_FREQUENCY
init|=
literal|"hbase.server.thread.wakefrequency"
decl_stmt|;
comment|/** Parameter name for HBase instance root directory */
specifier|static
specifier|final
name|String
name|HBASE_DIR
init|=
literal|"hbase.rootdir"
decl_stmt|;
comment|/** Default HBase instance root directory */
specifier|static
specifier|final
name|String
name|DEFAULT_HBASE_DIR
init|=
literal|"/hbase"
decl_stmt|;
comment|/** Used to construct the name of the directory in which a HRegion resides */
specifier|static
specifier|final
name|String
name|HREGIONDIR_PREFIX
init|=
literal|"hregion_"
decl_stmt|;
comment|/** Used to construct the name of the log directory for a region server */
specifier|static
specifier|final
name|String
name|HREGION_LOGDIR_NAME
init|=
literal|"log"
decl_stmt|;
comment|/** Name of old log file for reconstruction */
specifier|static
specifier|final
name|String
name|HREGION_OLDLOGFILE_NAME
init|=
literal|"oldlogfile.log"
decl_stmt|;
comment|/** Default maximum file size */
specifier|static
specifier|final
name|long
name|DEFAULT_MAX_FILE_SIZE
init|=
literal|64
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 64MB
comment|// Always store the location of the root table's HRegion.
comment|// This HRegion is never split.
comment|// region name = table + startkey + regionid. This is the row key.
comment|// each row in the root and meta tables describes exactly 1 region
comment|// Do we ever need to know all the information that we are storing?
comment|// Note that the name of the root table starts with "-" and the name of the
comment|// meta table starts with "." Why? it's a trick. It turns out that when we
comment|// store region names in memory, we use a SortedMap. Since "-" sorts before
comment|// "." (and since no other table name can start with either of these
comment|// characters, the root region will always be the first entry in such a Map,
comment|// followed by all the meta regions (which will be ordered by their starting
comment|// row key as well), followed by all user tables. So when the Master is
comment|// choosing regions to assign, it will always choose the root region first,
comment|// followed by the meta regions, followed by user regions. Since the root
comment|// and meta regions always need to be on-line, this ensures that they will
comment|// be the first to be reassigned if the server(s) they are being served by
comment|// should go down.
comment|/** The root table's name.*/
specifier|static
specifier|final
name|Text
name|ROOT_TABLE_NAME
init|=
operator|new
name|Text
argument_list|(
literal|"-ROOT-"
argument_list|)
decl_stmt|;
comment|/** The META table's name. */
specifier|static
specifier|final
name|Text
name|META_TABLE_NAME
init|=
operator|new
name|Text
argument_list|(
literal|".META."
argument_list|)
decl_stmt|;
comment|// Defines for the column names used in both ROOT and META HBase 'meta' tables.
comment|/** The ROOT and META column family (string) */
specifier|static
specifier|final
name|String
name|COLUMN_FAMILY_STR
init|=
literal|"info:"
decl_stmt|;
comment|/** The ROOT and META column family (Text) */
specifier|static
specifier|final
name|Text
name|COLUMN_FAMILY
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY_STR
argument_list|)
decl_stmt|;
comment|/** Array of meta column names */
specifier|static
specifier|final
name|Text
index|[]
name|COLUMN_FAMILY_ARRAY
init|=
operator|new
name|Text
index|[]
block|{
name|COLUMN_FAMILY
block|}
decl_stmt|;
comment|/** ROOT/META column family member - contains HRegionInfo */
specifier|static
specifier|final
name|Text
name|COL_REGIONINFO
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|"regioninfo"
argument_list|)
decl_stmt|;
comment|/** Array of column - contains HRegionInfo */
specifier|static
specifier|final
name|Text
index|[]
name|COL_REGIONINFO_ARRAY
init|=
operator|new
name|Text
index|[]
block|{
name|COL_REGIONINFO
block|}
decl_stmt|;
comment|/** ROOT/META column family member - contains HServerAddress.toString() */
specifier|static
specifier|final
name|Text
name|COL_SERVER
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|"server"
argument_list|)
decl_stmt|;
comment|/** ROOT/META column family member - contains server start code (a long) */
specifier|static
specifier|final
name|Text
name|COL_STARTCODE
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|"serverstartcode"
argument_list|)
decl_stmt|;
comment|/** the lower half of a split region */
specifier|static
specifier|final
name|Text
name|COL_SPLITA
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY_STR
operator|+
literal|"splitA"
argument_list|)
decl_stmt|;
comment|/** the upper half of a split region */
specifier|static
specifier|final
name|Text
name|COL_SPLITB
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY_STR
operator|+
literal|"splitB"
argument_list|)
decl_stmt|;
comment|// Other constants
comment|/**    * An empty instance of Text.    */
specifier|static
specifier|final
name|Text
name|EMPTY_TEXT
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
comment|/**    * Used by scanners, etc when they want to start at the beginning of a region    */
specifier|static
specifier|final
name|Text
name|EMPTY_START_ROW
init|=
name|EMPTY_TEXT
decl_stmt|;
comment|/** When we encode strings, we always specify UTF8 encoding */
specifier|static
specifier|final
name|String
name|UTF8_ENCODING
init|=
literal|"UTF-8"
decl_stmt|;
comment|/**    * Timestamp to use when we want to refer to the latest cell.    * This is the timestamp sent by clients when no timestamp is specified on    * commit.    */
specifier|static
specifier|final
name|long
name|LATEST_TIMESTAMP
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Define for 'return-all-versions'.    */
specifier|static
specifier|final
name|int
name|ALL_VERSIONS
init|=
operator|-
literal|1
decl_stmt|;
block|}
end_interface

end_unit

