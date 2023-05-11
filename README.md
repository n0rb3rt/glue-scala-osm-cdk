# Glue Scala OpenStreetMap Job with GeoMesa
This repository demonstrates a Glue job written in Scala that reads [OpenStreetMap Open Data on AWS](https://registry.opendata.aws/osm/) and uses [GeoMesa](https://www.geomesa.org/) to spatially partition data in S3 and visualize the results with [GeoServer](https://geoserver.org/).

### Spatial Partitioning
Partitioning data in a data lake is critical to achieving performance and scale so that queries may prune unnecessary folders and avoid scanning portions of data only to filter it out.  Many tools exist like Glue Crawlers and Data Catalog that aid in [effectively partitioning](https://aws.amazon.com/blogs/big-data/work-with-partitioned-data-in-aws-glue/) along common dimensions in data such as time or customer.  When working with geospatial data and queries, choosing an appropriate partioning scheme can achieve the same goals.  
### Z-Order Curve Index
A [z-order curve](https://en.wikipedia.org/wiki/Z-order_curve) maps multi-dimensional data, in this case y and x or latitude and longitude, into a single dimension.  These values can be used as keys in key-value data stores like NoSQL databases or in this case in S3 as partitioned folders of the spatial data.  When querying the data, input geometries like bounding boxes can also be translated into z-curve values, and are provided as predicates to the query that prevent irrelevant ranges of data from being scanned in the same way that a time window would exclude chronologically partitioned data falling outside the window.

<img src="https://www.geomesa.org/documentation/stable/_images/Zcurve-HiRes.png" width="400">

### GeoHash
A [geohash](https://en.wikipedia.org/wiki/Geohash) is one such application of a z-order curve and can be used in this type of partitioning.  AWS tools like Athena and Redshift have spatial functions and can also be used to [geospatially partition](https://aws.amazon.com/blogs/publicsector/how-partition-geospatial-data-lake-analysis-amazon-redshift/) data in S3 and work well especially for point-based (lat/lon) geometry.  When working with other types of geometry, however, challenges arise for data that may fall into more than one partition like when [working with OpenStreetMap data](https://aws.amazon.com/blogs/big-data/querying-openstreetmap-with-amazon-athena/).

<img src="https://images.ctfassets.net/3prze68gbwl1/assetglossary-17su9wok1ui0z7r/55420735eef7b0469e22092fdc0683f4/geohashing-large-scale-example.jpeg" width="400">

### GeoMesa
[GeoMesa](https://www.geomesa.org/) is an open source suite of tools that enables large-scale geospatial querying and analytics on distributed computing systems. It provides a convenient library for Spark that enables Spark SQL and Glue jobs to support geospatial functions and data.  And can write geospatially partitioned data into S3 and can be used in Jupyter Notebooks.  Stored data can also be read by GeoServer through a GeoMesa plugin.  This provides a powerful set of tools for performing large-scale geospatial analysis and serving the data to visualization tools like web applications, GIS packages, and interactive analysis.

<img src="https://www.geomesa.org/documentation/stable/_images/jupyter-leaflet.png" width="400">

### CDK
This project uses AWS CDK to build and provision an example Glue Job and an EC2 instance with GeoMesa-enabled GeoServer.

